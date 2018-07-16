package jason.asSemantics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.architecture.MindInspectorWeb;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithFunctionTerm;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Rule;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.asSyntax.directives.FunctionRegister;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import jason.bb.StructureWrapperForLiteral;
import jason.functions.Count;
import jason.functions.RuleToFunction;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;
import jason.runtime.SourcePath;
import jason.util.Config;



/**
 * The Agent class has the belief base and plan library of an
 * AgentSpeak agent. It also implements the default selection
 * functions of the AgentSpeak semantics.
 */
public class Agent {

    // Members
    protected BeliefBase       bb = null;
    protected PlanLibrary      pl = null;
    protected TransitionSystem ts = null;
    protected String           aslSource = null;

    private List<Literal>      initialGoals = null; // initial goals in the source code
    private List<Literal>      initialBels  = null; // initial beliefs in the source code

    private Map<String, InternalAction> internalActions = null;
    private Map<String, ArithFunction>  functions       = null;

    private boolean hasCustomSelOp = true;

    private static ScheduledExecutorService scheduler = null;

    //private QueryCache qCache = null;
    //private QueryCacheSimple qCache = null;
    //private QueryProfiling   qProfiling = null;

    protected Logger logger = Logger.getLogger(Agent.class.getName());

    public Agent() {
        checkCustomSelectOption();
    }

    /**
     * Setup the default agent configuration.
     *
     * Creates the agent class defined by <i>agClass</i>, default is jason.asSemantics.Agent.
     * Creates the TS for the agent.
     * Creates the belief base for the agent.
     */
    public static Agent create(AgArch arch, String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        try {
            Agent ag = (Agent) Class.forName(agClass).newInstance();

            new TransitionSystem(ag, null, stts, arch);

            BeliefBase bb = null;
            if (bbPars == null)
                bb = new DefaultBeliefBase();
            else
                bb = (BeliefBase) Class.forName(bbPars.getClassName()).newInstance();

            ag.setBB(bb);     // the agent's BB have to be already set for the BB initialisation
            ag.initAg();

            if (bbPars != null)
                bb.init(ag, bbPars.getParametersArray());
            ag.load(asSrc); // load the source code of the agent
            return ag;
        } catch (Exception e) {
            throw new JasonException("as2j: error creating the customised Agent class! - "+agClass, e);
        }
    }



    /** Initialises the TS and other components of the agent */
    public void initAg() {
        if (bb == null) bb = new DefaultBeliefBase();
        if (pl == null) pl = new PlanLibrary();

        if (initialGoals == null) initialGoals = new ArrayList<Literal>();
        if (initialBels  == null) initialBels  = new ArrayList<Literal>();

        if (internalActions == null) internalActions = new HashMap<String, InternalAction>();
        initDefaultFunctions();

        if (ts == null) ts = new TransitionSystem(this, null, null, new AgArch());

        //if (ts.getSettings().hasQueryCache()) qCache = new QueryCache(this);
        //if (ts.getSettings().hasQueryProfiling()) qProfiling = new QueryProfiling(this);
        //if (ts.getSettings().hasQueryCache())     qCache = new QueryCacheSimple(this, qProfiling);

        if (! "false".equals(Config.get().getProperty(Config.START_WEB_MI))) MindInspectorWeb.get().registerAg(this);
    }


    /** parse and load the agent code, asSrc may be null */
    public void initAg(String asSrc) throws JasonException {
        initAg();
        load(asSrc);
    }

    /** parse and load the initial agent code, asSrc may be null */
    public void load(String asSrc) throws JasonException {
        // set the agent
        try {
            boolean parsingOk = true;
            if (asSrc != null) {
                asSrc = asSrc.replaceAll("\\\\", "/");
                setASLSrc(asSrc);

                if (asSrc.startsWith(SourcePath.CRPrefix)) {
                    // loads the class from a jar file (for example)
                    parseAS(Agent.class.getResource(asSrc.substring(SourcePath.CRPrefix.length())).openStream());
                } else {
                    // check whether source is an URL string
                    try {
                        parsingOk = parseAS(new URL(asSrc));
                    } catch (MalformedURLException e) {
                        parsingOk = parseAS(new File(asSrc));
                    }
                }
            }


            if (parsingOk) {
                if (getPL().hasMetaEventPlans())
                    getTS().addGoalListener(new GoalListenerForMetaEvents(getTS()));

                addInitialBelsFromProjectInBB();
                addInitialBelsInBB();
                addInitialGoalsFromProjectInBB();
                addInitialGoalsInTS();
                fixAgInIAandFunctions(this); // used to fix agent reference in functions used inside includes
            }

            loadKqmlPlans();
            addInitialBelsInBB(); // in case kqml plan file has some belief 

            setASLSrc(asSrc);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating customised Agent class!", e);
            throw new JasonException("Error creating customised Agent class! - " + e);
        }
    }


    public void loadKqmlPlans() {
        // load kqml plans at the end of the ag PL
        Config c = Config.get();
        if (c.getKqmlPlansFile().equals(Message.kqmlDefaultPlans)) {
            // load default implementation
            // if KQML functor is not changed
            if (c.getKqmlFunctor().equals(Message.kqmlReceivedFunctor)) {
                String file = Message.kqmlDefaultPlans.substring(Message.kqmlDefaultPlans.indexOf("/"));
                if (JasonException.class.getResource(file) != null) {
                    setASLSrc("kqmlPlans.asl");
                    parseAS(JasonException.class.getResource(file));
                } else {
                    logger.warning("The kqmlPlans.asl was not found!");
                }
            }
        } else {
            // load from specified file
            try {
                parseAS(new File(c.getKqmlPlansFile()));
            } catch (Exception e) {
                logger.warning("Error reading kqml semantic plans. "+e+". from file "+c.getKqmlPlansFile());
            }
        }
    }

    /** @deprecated Prefer the initAg method with only the source code of the agent as parameter.
     *
     *  A call of this method like
     *     <pre>
     *     TransitionSystem ts = ag.initAg(arch, bb, asSrc, stts)
     *     </pre>
     *  can be replaced by
     *     <pre>
     *     new TransitionSystem(ag, new Circumstance(), stts, arch);
     *     ag.setBB(bb); // only if you use a custom BB
     *     ag.initAg(asSrc);
     *     TransitionSystem ts = ag.getTS();
     *     </pre>
     */
    public TransitionSystem initAg(AgArch arch, BeliefBase bb, String asSrc, Settings stts) throws JasonException {
        try {
            if (bb != null)
                setBB(bb);
            new TransitionSystem(this, null, stts, arch);
            initAg(asSrc);
            return ts;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating the agent class!", e);
            throw new JasonException("Error creating the agent class! - " + e);
        }
    }

    public void stopAg() {
        synchronized (bb.getLock()) {
            bb.stop();
        }

        //if (qProfiling != null)
        //    qProfiling.show();

        //if (scheduler != null)
        //    scheduler.shutdownNow();

        for (InternalAction ia: internalActions.values()) {
            try {
                ia.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (MindInspectorWeb.isRunning()) {
            MindInspectorWeb.get().removeAg(this);
        }
    }

    /**
     *  Clone BB, PL, Circumstance.
     *  A new TS is created (based on the cloned circumstance).
     */
    public Agent clone(AgArch arch) {
        Agent a = null;
        try {
            a = this.getClass().newInstance();
        } catch (InstantiationException e1) {
            logger.severe(" cannot create derived class" +e1);
            return null;
        } catch (IllegalAccessException e2) {
            logger.severe(" cannot create derived class" +e2);
            return null;
        }

        a.setLogger(arch);
        if (this.getTS().getSettings().verbose() >= 0)
            a.logger.setLevel(this.getTS().getSettings().logLevel());

        synchronized (getBB().getLock()) {
            a.bb = this.bb.clone();
        }
        a.pl = this.pl.clone();
        try {
            fixAgInIAandFunctions(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        a.aslSource = this.aslSource;
        a.internalActions = new HashMap<String, InternalAction>();
        a.setTS(new TransitionSystem(a, this.getTS().getC().clone(), this.getTS().getSettings(), arch));
        if (a.getPL().hasMetaEventPlans())
            a.getTS().addGoalListener(new GoalListenerForMetaEvents(a.getTS()));

        a.initAg(); //for initDefaultFunctions() and for overridden/custom agent
        return a;
    }

    private void fixAgInIAandFunctions(Agent a) throws Exception {
        // find all internal actions and functions and change the pointer for agent
        synchronized (getPL().getLock()) {
            for (Plan p: a.getPL()) {
                // search context
                if (p.getContext() instanceof Literal)
                    fixAgInIAandFunctions(a, (Literal)p.getContext());

                // search body
                if (p.getBody() instanceof Literal)
                    fixAgInIAandFunctions(a, (Literal)p.getBody());
            }
        }
    }

    private void fixAgInIAandFunctions(Agent a, Literal l) throws Exception {
        // if l is internal action/function
        if (l instanceof InternalActionLiteral) {
            ((InternalActionLiteral)l).setIA(null); // reset the IA in the literal, the IA there will be updated next getIA call
        }
        if (l instanceof ArithFunctionTerm) {
            ((ArithFunctionTerm)l).setAgent(a);
        }
        if (l instanceof Rule) {
            LogicalFormula f = ((Rule)l).getBody();
            if (f instanceof Literal) {
                fixAgInIAandFunctions(a, (Literal)f);
            }
        }
        for (int i=0; i<l.getArity(); i++) {
            if (l.getTerm(i) instanceof Literal)
                fixAgInIAandFunctions(a, (Literal)l.getTerm(i));
        }
    }

    public void setLogger(AgArch arch) {
        if (arch != null)
            logger = Logger.getLogger(Agent.class.getName() + "." + arch.getAgName());
    }

    public Logger getLogger() {
        return logger;
    }

    public static synchronized ScheduledExecutorService getScheduler() {
        if (scheduler == null) {
            int n;
            try {
                n = new Integer( Config.get().get(Config.NB_TH_SCH).toString() );
            } catch (Exception e) {
                n = 2;
            }
            scheduler = Executors.newScheduledThreadPool(n);
        }
        return scheduler;
    }


    /** Returns the .asl file source used to create this agent */
    public String getASLSrc() {
        return aslSource;
    }

    public void setASLSrc(String file) {
        if (file != null && file.startsWith("./"))
            file = file.substring(2);
        aslSource = file;
    }

    /** Adds beliefs and plans form an URL */
    public boolean parseAS(URL asURL) {
        try {
            parseAS(asURL.openStream());
            logger.fine("as2j: AgentSpeak program '" + asURL + "' parsed successfully!");
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "as2j: the AgentSpeak source file '"+asURL+"' was not found!");
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "as2j: parsing error: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "as2j: parsing error: \"" + asURL + "\"", e);
        }
        return false;
    }

    /** Adds beliefs and plans form a file */
    public boolean parseAS(File asFile) {
        try {
            parseAS(new FileInputStream(asFile));
            logger.fine("as2j: AgentSpeak program '" + asFile + "' parsed successfully!");
            return true;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "as2j: the AgentSpeak source file '"+asFile+"' was not found!");
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "as2j: parsing error:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "as2j: error parsing \"" + asFile + "\"", e);
        }
        return false;
    }

    public void parseAS(InputStream asIn) throws ParseException, JasonException {
        as2j parser = new as2j(asIn);
        parser.agent(this);
    }
    public void parseAS(Reader asIn) throws ParseException, JasonException {
        as2j parser = new as2j(asIn);
        parser.agent(this);
    }

    @SuppressWarnings("unchecked")
    public InternalAction getIA(String iaName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (iaName.charAt(0) == '.')
            iaName = "jason.stdlib" + iaName;
        InternalAction objIA = internalActions.get(iaName);
        if (objIA == null) {
            @SuppressWarnings("rawtypes")
            Class iaclass = Class.forName(iaName);
            try {
                // check if the class has "create" method -- singleton implementation
                Method create = iaclass.getMethod("create", (Class[])null);
                objIA = (InternalAction)create.invoke(null, (Object[])null);
            } catch (Exception e) {
                objIA = (InternalAction)iaclass.newInstance();
            }
            internalActions.put(iaName, objIA);
        }
        return objIA;
    }

    public void setIA(String iaName, InternalAction ia) {
        internalActions.put(iaName, ia);
    }

    public void initDefaultFunctions() {
        if (functions == null)
            functions = new HashMap<String, ArithFunction>();
        addFunction(Count.class, false);
    }

    /** register an arithmetic function implemented in Java */
    public void addFunction(Class<? extends ArithFunction> c) {
        addFunction(c,true);
    }
    /** register an arithmetic function implemented in Java */
    private void addFunction(Class<? extends ArithFunction> c, boolean user) {
        try {
            ArithFunction af = c.newInstance();
            String error = null;
            if (user)
                error = FunctionRegister.checkFunctionName(af.getName());
            if (error != null)
                logger.warning(error);
            else
                functions.put(af.getName(),af);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registering function "+c.getName(),e);
        }
    }

    /** register an arithmetic function implemented in AS (by a rule, literal, or internal action) */
    public void addFunction(String function, int arity, String literal) {
        try {
            String error = FunctionRegister.checkFunctionName(function);
            if (error != null)
                logger.warning(error);
            else
                functions.put(function, new RuleToFunction(literal, arity));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registering function "+literal,e);
        }
    }

    /** get the object the implements the arithmetic function <i>function/arity</i>,
     *  either global (like math.max) or local (like .count).
     */
    public ArithFunction getFunction(String function, int arity) {
        if (functions == null) return null;
        ArithFunction af = functions.get(function);
        if (af == null || !af.checkArity(arity))
            // try global function
            af = FunctionRegister.getFunction(function, arity);
        if (af != null && af.checkArity(arity))
            return af;
        else
            return null;
    }

    /** Belief b will be stored to be included as an ordinary belief when the agent will start running.
     *  This method is usually called by the parser; at compile time, when the TS may not be ready yet and thus
     *  no events can be produced. Beliefs are then scheduled here to be definitely included later when the
     *  TS is ready. */
    public void addInitialBel(Literal b) {
        initialBels.add(b);
    }
    public List<Literal> getInitialBels() {
        return initialBels;
    }

    /** add the initial beliefs in BB and produce the corresponding events */
    public void addInitialBelsInBB() throws RevisionFailedException {
        // Once beliefs are stored in a Stack in the BB, insert them in inverse order
        for (int i=initialBels.size()-1; i >=0; i--)
            addInitBel(initialBels.get(i));
        initialBels.clear();
    }

    protected void addInitialBelsFromProjectInBB() {
        String sBels = getTS().getSettings().getUserParameter(Settings.INIT_BELS);
        if (sBels != null)
            try {
                for (Term t: ASSyntax.parseList("["+sBels+"]"))
                    addInitBel( ((Literal)t).forceFullLiteralImpl());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Initial beliefs from project '["+sBels+"]' is not a list of literals.");
            }
    }

    private void addInitBel(Literal b) throws RevisionFailedException {
        // if l is not a rule and has free vars (like l(X)), convert it into a rule like "l(X) :- true."
        if (!b.isRule() && !b.isGround())
            b = new Rule(b,Literal.LTrue);
        if (!b.hasSource()) // so that rules also have source
            b.addAnnot(BeliefBase.TSelf);

        // does not do BRF for rules (and so do not produce events +bel for rules)
        if (b.isRule()) {
            getBB().add(b);
        } else {
            b = (Literal)b.capply(null); // to solve arithmetic expressions
            addBel(b);
        }
    }


    /** goal g will be stored to be included as an initial goal when the agent will start running */
    public void addInitialGoal(Literal g) {
        initialGoals.add(g);
    }

    public Collection<Literal> getInitialGoals() {
        return initialGoals;
    }

    /** includes all initial goals in the agent reasoner */
    public void addInitialGoalsInTS() {
        for (Literal g: initialGoals) {
            g.makeVarsAnnon();
            if (! g.hasSource())
                g.addAnnot(BeliefBase.TSelf);
            getTS().getC().addAchvGoal(g,Intention.EmptyInt);
        }
        initialGoals.clear();
    }

    protected void addInitialGoalsFromProjectInBB() {
        String sGoals = getTS().getSettings().getUserParameter(Settings.INIT_GOALS);
        if (sGoals != null) {
            try {
                for (Term t: ASSyntax.parseList("["+sGoals+"]")) {
                    Literal g = ((Literal)t).forceFullLiteralImpl();
                    g.makeVarsAnnon();
                    if (! g.hasSource())
                        g.addAnnot(BeliefBase.TSelf);
                    getTS().getC().addAchvGoal(g,Intention.EmptyInt);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Initial goals from project '["+sGoals+"]' is not a list of literals.");
            }
        }
    }


    /** Imports beliefs, plans and initial goals from another agent. Initial beliefs and goals
     *  are stored in "initialBels" and "initialGoals" lists but not included in the BB / TS.
     *  The methods addInitialBelsInBB and addInitialGoalsInTS should be called in the sequel to
     *  add those beliefs and goals into the agent. */
    public void importComponents(Agent a) throws JasonException {
        if (a != null) {
            for (Literal b: a.initialBels) {
                this.addInitialBel(b);
                try {
                    fixAgInIAandFunctions(this, b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (Literal g: a.initialGoals)
                this.addInitialGoal(g);

            for (Plan p: a.getPL())
                this.getPL().add(p, false);
            // reset Ag in internal actions of plans
            try {
                fixAgInIAandFunctions(this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (getPL().hasMetaEventPlans())
                getTS().addGoalListener(new GoalListenerForMetaEvents(getTS()));
        }
    }


    /**
     * Follows the default implementation for the agent's message acceptance
     * relation and selection functions
     */
    public boolean socAcc(Message m) {
        return true;
    }

    /** Returns true if this agent accepts to be killed by another agent called <i>agName</i>.
     *  This method can be overridden to customize this option. */
    public boolean killAcc(String agName) {
        //System.out.println("I am being killed by "+agName+", but that is ok...");
        return true;
    }

    public Event selectEvent(Queue<Event> events) {
        // make sure the selected Event is removed from 'events' queue
        return events.poll();
    }

    public Option selectOption(List<Option> options) {
        if (options != null && !options.isEmpty()) {
            return options.remove(0);
        } else {
            return null;
        }
    }

    public Intention selectIntention(Queue<Intention> intentions) {
        // make sure the selected Intention is removed from 'intentions'
        // and make sure no intention will "starve"!!!
        return intentions.poll();
    }

    public Message selectMessage(Queue<Message> messages) {
        // make sure the selected Message is removed from 'messages'
        return messages.poll();
    }

    public ActionExec selectAction(List<ActionExec> actList) {
        // make sure the selected Action is removed from actList
        // (do not return suspended intentions)
        synchronized (actList) {
            Iterator<ActionExec> i = actList.iterator();
            while (i.hasNext()) {
                ActionExec a = i.next();
                if (!a.getIntention().isSuspended()) {
                    i.remove();
                    return a;
                }
            }
        }
        return null;
    }

    /** TS Initialisation (called by the AgArch) */
    public void setTS(TransitionSystem ts) {
        this.ts = ts;
        setLogger(ts.getUserAgArch());
        if (ts.getSettings().verbose() >= 0)
            logger.setLevel(ts.getSettings().logLevel());
    }

    public TransitionSystem getTS() {
        return ts;
    }

    public void setBB(BeliefBase bb) {
        this.bb = bb;
    }
    public BeliefBase getBB() {
        return bb;
    }

    public void setPL(PlanLibrary pl) {
        this.pl = pl;
    }

    public PlanLibrary getPL() {
        return pl;
    }

    /** Belief Update Function: adds/removes percepts into belief base.
     *
     *  @return the number of changes (add + dels)
     */
    public int buf(Collection<Literal> percepts) {
        /*
        // complexity 3n

        HashSet percepts = clone from the list of current environment percepts // 1n

        for b in BBPercept (the set of perceptions already in BB) // 1n
            if b not in percepts // constant time test
                remove b in BBPercept // constant time
                remove b in percept

        for p still in percepts // 1n
            add p in BBPercepts
        */

        if (percepts == null) {
            return 0;
        }

        // stat
        int adds = 0;
        int dels = 0;
        //long startTime = qProfiling == null ? 0 : System.nanoTime();

        // to copy percepts allows the use of contains below
        Set<StructureWrapperForLiteral> perW = new HashSet<StructureWrapperForLiteral>();
        Iterator<Literal> iper = percepts.iterator();
        while (iper.hasNext())
            perW.add(new StructureWrapperForLiteral(iper.next()));


        // deleting percepts in the BB that are not perceived anymore
        Iterator<Literal> perceptsInBB = getBB().getPercepts();
        while (perceptsInBB.hasNext()) {
            Literal l = perceptsInBB.next();
            if (l.subjectToBUF() && ! perW.remove(new StructureWrapperForLiteral(l))) { // l is not perceived anymore
                dels++;
                perceptsInBB.remove(); // remove l as perception from BB

                // new version (it is sure that l is in BB, only clone l when the event is relevant)
                Trigger te = new Trigger(TEOperator.del, TEType.belief, l);
                if (ts.getC().hasListener() || pl.hasCandidatePlan(te)) {
                    l = ASSyntax.createLiteral(l.getFunctor(), l.getTermsArray());
                    l.addAnnot(BeliefBase.TPercept);
                    te.setLiteral(l);
                    ts.getC().addEvent(new Event(te, Intention.EmptyInt));
                }
            }
        }

        /*
            ////// previous version, without perW hashset
            // could not use percepts.contains(l), since equalsAsTerm must be
            // used (to ignore annotations)
            boolean wasPerceived = false;
            Iterator<Literal> ip = percepts.iterator();
            while (ip.hasNext()) {
                Literal t = ip.next();

                // if perception t is already in BB
                if (l.equalsAsStructure(t) && l.negated() == t.negated()) {
                    wasPerceived = true;
                    // remove in percepts, since it already is in BB
                    // [can not be always removed, since annots in this percepts should be added in BB
                    //  Jason team for AC, for example, use annots in perceptions]
                    if (!l.hasAnnot())
                        ip.remove();
                    break;
                }
            }
            if (!wasPerceived) {
                dels++;
                // new version (it is sure that l is in BB, only clone l when the event is relevant)
                perceptsInBB.remove(); // remove l as perception from BB

                Trigger te = new Trigger(TEOperator.del, TEType.belief, l);
                if (ts.getC().hasListener() || pl.hasCandidatePlan(te)) {
                    l = l.copy();
                    l.clearAnnots();
                    l.addAnnot(BeliefBase.TPercept);
                    te.setLiteral(l);
                    ts.getC().addEvent(new Event(te, Intention.EmptyInt));
                }
        */
        /*
        // even older version
        // can not delete l, but l[source(percept)]
        l = (Literal)l.clone();
        l.clearAnnots();
        l.addAnnot(BeliefBase.TPercept);
        if (bb.remove(l)) {
            ts.updateEvents(new Event(new Trigger(TEOperator.del, TEType.belief, l), Intention.EmptyInt));
        }
        */
        //}
        //}

        for (StructureWrapperForLiteral lw: perW) {
            try {
                Literal lp = lw.getLiteral().copy().forceFullLiteralImpl();
                lp.addAnnot(BeliefBase.TPercept);
                if (getBB().add(lp)) {
                    adds++;
                    ts.updateEvents(new Event(new Trigger(TEOperator.add, TEType.belief, lp), Intention.EmptyInt));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error adding percetion " + lw.getLiteral(), e);
            }
        }

        //if (qCache != null)
        //    qCache.reset();
        //if (qProfiling != null)
        //    qProfiling.newUpdateCycle(getTS().getUserAgArch().getCycleNumber(), adds+dels, System.nanoTime()-startTime);
        return adds + dels;
    }


    /*
    public QueryCacheSimple getQueryCache() {
        return qCache;
    }
    public QueryProfiling getQueryProfiling() {
        return qProfiling;
    }
    */

    /**
     * Returns true if BB contains the literal <i>bel</i> (using unification to test).
     * The unifier <i>un</i> is updated by the method.
     */
    public boolean believes(LogicalFormula bel, Unifier un) {
        try {
            Iterator<Unifier> iun = bel.logicalConsequence(this, un);
            if (iun != null && iun.hasNext()) {
                un.compose(iun.next());
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "** Error in method believes("+bel+","+un+").",e);
        }
        return false;
    }

    /**
     * Find a literal in BB using unification to test.
     *
     * Returns the belief as it is in BB, e.g. findBel(a(_),...)
     * may returns a(10)[source(ag)].
     *
     * The unifier <i>un</i> is updated by the method.
     */
    public Literal findBel(Literal bel, Unifier un) {
        synchronized (bb.getLock()) {
            Iterator<Literal> relB = bb.getCandidateBeliefs(bel, un);
            if (relB != null) {
                while (relB.hasNext()) {
                    Literal b = relB.next();

                    // recall that order is important because of annotations!
                    if (!b.isRule() && un.unifies(bel, b)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }


    /**
     * This function should revise the belief base with the given literal to
     * add, to remove, and the current intention that triggered the operation.
     *
     * <p>In its return, List[0] has the list of actual additions to
     * the belief base, and List[1] has the list of actual deletions;
     * this is used to generate the appropriate internal events. If
     * nothing change, returns null.
     */
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel,  Intention i) throws RevisionFailedException {
        return brf(beliefToAdd, beliefToDel, i, false);
    }

    /**
     * This function should revise the belief base with the given literal to
     * add, to remove, and the current intention that triggered the operation.
     *
     * <p>In its return, List[0] has the list of actual additions to
     * the belief base, and List[1] has the list of actual deletions;
     * this is used to generate the appropriate internal events. If
     * nothing change, returns null.
     */
    @SuppressWarnings("unchecked")
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel,  Intention i, boolean addEnd) throws RevisionFailedException {
        // This class does not implement belief revision! It
        // is supposed that a subclass will do it.
        // It simply add/del the belief.

        int position = 0; // add in the begin
        if (addEnd)
            position = 1;

        List<Literal>[] result = null;
        synchronized (bb.getLock()) {
            try {
                if (beliefToAdd != null) {
                    if (logger.isLoggable(Level.FINE)) logger.fine("Doing (add) brf for " + beliefToAdd);

                    if (getBB().add(position, beliefToAdd)) {
                        result = new List[2];
                        result[0] = Collections.singletonList(beliefToAdd);
                        result[1] = Collections.emptyList();
                    }
                }

                if (beliefToDel != null) {
                    Unifier u = null;
                    try {
                        u = i.peek().unif; // get from current intention
                    } catch (Exception e) {
                        u = new Unifier();
                    }

                    if (logger.isLoggable(Level.FINE)) logger.fine("Doing (del) brf for " + beliefToDel + " in BB=" + believes(beliefToDel, u));

                    boolean removed = getBB().remove(beliefToDel);
                    if (!removed && !beliefToDel.isGround()) { // then try to unify the parameter with a belief in BB
                        if (believes(beliefToDel, u)) {
                            beliefToDel = (Literal)beliefToDel.capply(u);
                            removed = getBB().remove(beliefToDel);
                        }
                    }

                    if (removed) {
                        if (logger.isLoggable(Level.FINE)) logger.fine("Removed:" + beliefToDel);
                        if (result == null) {
                            result = new List[2];
                            result[0] = Collections.emptyList();
                        }
                        result[1] = Collections.singletonList(beliefToDel);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error at BRF.",e);
            }
        }
        return result;
    }

    /**
     * Adds <i>bel</i> in belief base (calling brf) and generates the
     * events. If <i>bel</i> has no source, add
     * <code>source(self)</code>. (the belief is not cloned!)
     */
    public boolean addBel(Literal bel) throws RevisionFailedException {
        if (!bel.hasSource()) {
            bel.addAnnot(BeliefBase.TSelf);
        }
        List<Literal>[] result = brf(bel, null, Intention.EmptyInt);
        if (result != null && ts != null) {
            ts.updateEvents(result, Intention.EmptyInt);
            return true;
        } else {
            return false;
        }
    }

    /**
     * If the agent believes in <i>bel</i>, removes it (calling brf)
     * and generate the event.
     */
    public boolean delBel(Literal bel) throws RevisionFailedException {
        if (!bel.hasSource()) {
            bel.addAnnot(BeliefBase.TSelf);
        }
        List<Literal>[] result = brf(null, bel, Intention.EmptyInt);
        if (result != null && ts != null) {
            ts.updateEvents(result, Intention.EmptyInt);
            return true;
        } else {
            return false;
        }
    }

    /** Removes all occurrences of <i>bel</i> in BB.
        If <i>un</i> is null, an empty Unifier is used.
     */
    public void abolish(Literal bel, Unifier un) throws RevisionFailedException {
        List<Literal> toDel = new ArrayList<Literal>();
        if (un == null) un = new Unifier();
        synchronized (bb.getLock()) {
            Iterator<Literal> il = getBB().getCandidateBeliefs(bel, un);
            if (il != null) {
                while (il.hasNext()) {
                    Literal inBB = il.next();
                    if (!inBB.isRule()) {
                        // need to clone unifier since it is changed in previous iteration
                        if (un.clone().unifiesNoUndo(bel, inBB)) {
                            toDel.add(inBB);
                        }
                    }
                }
            }

            for (Literal l: toDel) {
                delBel(l);
            }
        }
    }

    private void checkCustomSelectOption() {
        hasCustomSelOp = false;
        for (Method m: this.getClass().getMethods()) {
            if (!m.getDeclaringClass().equals(Agent.class) && m.getName().equals("selectOption")) {
                hasCustomSelOp = true;
            }
        }
    }

    public boolean hasCustomSelectOption() {
        return hasCustomSelOp;
    }

    static DocumentBuilder builder = null;

    /** Gets the agent "mind" (beliefs, plans, and circumstance) as XML */
    public Document getAgState() {
        if (builder == null) {
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating XML builder\n");
                return null;
            }
        }
        Document document = builder.newDocument();
        document.appendChild(document.createProcessingInstruction("xml-stylesheet", "href='http://jason.sf.net/xml/agInspection.xsl' type='text/xsl' "));

        Element ag = getAsDOM(document);
        document.appendChild(ag);

        ag.appendChild(ts.getC().getAsDOM(document));
        return document;
    }

    @Override
    public String toString() {
        return "Agent "+getASLSrc();
    }

    /** Gets the agent "mind" as XML */
    public Element getAsDOM(Document document) {
        Element ag = (Element) document.createElement("agent");
        ag.setAttribute("name", ts.getUserAgArch().getAgName());
        ag.setAttribute("cycle", ""+ts.getUserAgArch().getCycleNumber());

        ag.appendChild(bb.getAsDOM(document));
        // ag.appendChild(ps.getAsDOM(document));
        return ag;
    }

    /** Gets the agent program (Beliefs and plans) as XML */
    public Document getAgProgram() {
        if (builder == null) {
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating XML builder\n");
                return null;
            }
        }
        Document document = builder.newDocument();
        Element ag = (Element) document.createElement("agent");
        if (getASLSrc() != null && getASLSrc().length() > 0) {
            ag.setAttribute("source", getASLSrc());
        }
        ag.appendChild(bb.getAsDOM(document));
        ag.appendChild(pl.getAsDOM(document));
        document.appendChild(ag);

        return document;
    }

}
