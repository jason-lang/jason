package jason.asSemantics;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.architecture.MindInspectorWeb;
import jason.asSyntax.*;
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
import jason.pl.PlanLibrary;
import jason.runtime.Settings;
import jason.runtime.SourcePath;
import jason.util.Config;
import jason.util.ToDOM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * The Agent class has the belief base and plan library of an
 * AgentSpeak agent. It also implements the default selection
 * functions of the AgentSpeak semantics.
 */
public class Agent implements Serializable, ToDOM {

    @Serial
    private static final long serialVersionUID = -2628324957954474455L;

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

    protected transient Logger logger = Logger.getLogger(Agent.class.getName());

    public Agent() {
        checkCustomSelectOption();
    }

    /**
     * Set up the default agent configuration.
     * <p>
     * Creates the agent class defined by <i>agClass</i>, default is jason.asSemantics.Agent.
     * Creates the TS for the agent.
     * Creates the belief base for the agent.
     */
    public static Agent create(AgArch arch, String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws Exception {
        //try {
            Agent ag = (Agent) Class.forName(agClass).getConstructor().newInstance();

            new TransitionSystem(ag, null, stts, arch);

            BeliefBase bb;
            if (bbPars == null)
                bb = new DefaultBeliefBase();
            else
                bb = (BeliefBase) Class.forName(bbPars.getClassName()).getConstructor().newInstance();

            ag.setBB(bb);     // the agent's BB have to be already set for the BB initialisation
            ag.initAg();

            if (bbPars != null)
                bb.init(ag, bbPars.getParametersArray());
            ag.loadInitialAS(asSrc); // load the source code of the agent
            return ag;
        //} catch (Exception e) {
        //    throw new JasonException("as2j: error creating the customised Agent class! - "+agClass, e);
        //}
    }

    private boolean considerToAddMIForThisAgent = true;
    public void setConsiderToAddMIForThisAgent(boolean add) {
        considerToAddMIForThisAgent = add;
    }

    /** Initialises the TS and other components of the agent */
    public void initAg() {
        if (bb == null) bb = new DefaultBeliefBase();
        if (pl == null) pl = new PlanLibrary();

        if (initialGoals == null) initialGoals = new ArrayList<>();
        if (initialBels  == null) initialBels  = new ArrayList<>();

        if (internalActions == null) internalActions = new HashMap<>();
        initDefaultFunctions();

        if (ts == null) ts = new TransitionSystem(this, null, null, new AgArch());

        //if (ts.getSettings().hasQueryCache()) qCache = new QueryCache(this);
        //if (ts.getSettings().hasQueryProfiling()) qProfiling = new QueryProfiling(this);
        //if (ts.getSettings().hasQueryCache())     qCache = new QueryCacheSimple(this, qProfiling);

        if (considerToAddMIForThisAgent)
            addToMindInspectorWeb();
    }

    public void addToMindInspectorWeb() {
        if (! "false".equals(Config.get().getProperty(Config.START_WEB_MI)))
            MindInspectorWeb.get().registerAg(this);
    }

    /** parse and load the initial agent code + kqml plans + project bels and goals, asSrc may be null */
    public void loadInitialAS(String asSrc) throws Exception {
        loadAS(asSrc);

        addInitialBelsFromProjectInBB();
        addInitialBelsInBB();
        addInitialGoalsFromProjectInBB();
        addInitialGoalsInTS();
        fixAgInIAandFunctions(this); // used to fix agent reference in functions used inside includes

        loadKqmlPlans();
        addInitialBelsInBB(); // in case kqml plan file has some belief
    }

    /**
     * parse and load some agent code, asSrc may be null
     * it does not load kqml default plans and does not trigger initial beliefs and goals
     */
    public void loadAS(String asSrc) throws Exception {
        if (asSrc != null && !asSrc.isEmpty()) {
            asSrc = asSrc.replaceAll("\\\\", "/");

            if (asSrc.startsWith(SourcePath.CRPrefix)) {
                // loads the class from a jar file (for example)
                parseAS(Agent.class.getResource(asSrc.substring(SourcePath.CRPrefix.length())).openStream() , asSrc);
            } else {
                // check whether source is a URL string
                try {
                    parseAS(new URL(asSrc));
                } catch (MalformedURLException e) {
                    parseAS(new File(asSrc));
                }
            }
        }

        if (getPL().hasMetaEventPlans())
            getTS().addGoalListener(new GoalListenerForMetaEvents(getTS()));

        setASLSrc(asSrc);
    }

    /** parse and load asl code */
    /*public void load(InputStream in, String sourceId) throws Exception {
        parseAS(in, sourceId);

        if (getPL().hasMetaEventPlans())
            getTS().addGoalListener(new GoalListenerForMetaEvents(getTS()));

        addInitialBelsInBB();
        addInitialGoalsInTS();
        fixAgInIAandFunctions(this); // used to fix agent reference in functions used inside includes
    }*/


    public void loadKqmlPlans() throws Exception {
        // load kqml plans at the end of the ag PL
        Config c = Config.get();
        if (c.getKqmlPlansFile().equals(Message.kqmlDefaultPlans)) {
            // load default implementation
            // if KQML functor is not changed
            if (c.getKqmlFunctor().equals(Message.kqmlReceivedFunctor)) {
                String file = Message.kqmlDefaultPlans.substring(Message.kqmlDefaultPlans.indexOf("/"));
                if (JasonException.class.getResource(file) != null) {
                    parseAS(JasonException.class.getResource(file), PlanLibrary.KQML_PLANS_FILE); // the kqmlPlans.asl argument should be used here (see hasUserKqmlReceived in PlanLibrary)
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

    /**
     * Clear Agent's Beliefs and Plan Library
     */
    public void clearAgMemory() {
        if (bb != null) bb.clear();
        if (pl != null) pl.clear();
    }

    public void stopAg() {
        bb.getLock().lock();
        try {
            bb.stop();
        } finally {
            bb.getLock().unlock();
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
        Agent a;
        try {
            a = this.getClass().getConstructor().newInstance();
        } catch (InstantiationException e1) {
            logger.severe(" cannot create derived class" + e1);
            return null;
        } catch (Exception e2) {
            logger.severe(" cannot create derived class" + e2);
            return null;
        }
        return cloneInto(arch, a);
    }

    public Agent cloneInto(AgArch arch, Agent a) {
        a.setLogger(arch);
        if (this.getTS().getSettings().verbose() >= 0)
            a.logger.setLevel(this.getTS().getSettings().logLevel());

        a.bb = this.bb.clone();

        a.pl = this.pl.clone();
        try {
            fixAgInIAandFunctions(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        a.aslSource = this.aslSource;
        a.internalActions = new HashMap<>();
        a.setTS(new TransitionSystem(a,
                this.getTS().getC().clone(),
                this.getTS().getSettings(),
                arch));
        a.getTS().setLogger(arch);
        if (a.getPL().hasMetaEventPlans())
            a.getTS().addGoalListener(new GoalListenerForMetaEvents(a.getTS()));

        a.initAg(); // for initDefaultFunctions() and for overridden/custom agent
        return a;
    }

    private void fixAgInIAandFunctions(Agent a) throws Exception {
        // find all internal actions and functions and change the pointer for agent
        getPL().getLock().lock();
        try {
            for (Plan p: a.getPL()) {
                // search context
                if (p.getContext() instanceof Literal l)
                    fixAgInIAandFunctions(a, l);

                // search body
                if (p.getBody() instanceof Literal l)
                    fixAgInIAandFunctions(a, l);
            }
        } finally {
            getPL().getLock().unlock();
        }
    }

    private void fixAgInIAandFunctions(Agent a, Literal l) throws Exception {
        // if l is internal action/function
        if (l instanceof InternalActionLiteral ia) {
            ia.setIA(null); // reset the IA in the literal, the IA there will be updated next getIA call
        }
        if (l instanceof ArithFunctionTerm af) {
            af.setAgent(a);
        }
        if (l instanceof Rule r) {
            LogicalFormula f = r.getBody();
            if (f instanceof Literal fl) {
                fixAgInIAandFunctions(a, fl);
            }
        }
        for (int i=0; i<l.getArity(); i++) {
            if (l.getTerm(i) instanceof Literal tl)
                fixAgInIAandFunctions(a, tl);
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
                n = Integer.parseInt( Config.get().get(Config.NB_TH_SCH).toString() );
            } catch (Exception e) {
                n = 3;
            }
            scheduler = Executors.newScheduledThreadPool(n, Thread.ofVirtual().factory());
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
    public void parseAS(URL asURL) throws Exception {
        parseAS(asURL, asURL.toString());
    }
    public void parseAS(URL asURL, String sourceId) throws Exception {
        parseAS(asURL.openStream(), sourceId);
        logger.fine("as2j: AgentSpeak program '" + asURL + "' parsed successfully!");
    }

    /** Adds beliefs and plans form a file */
    public void parseAS(File asFile) throws Exception {
        parseAS(new FileInputStream(asFile), asFile.getName());
        logger.fine("as2j: AgentSpeak program '" + asFile + "' parsed successfully!");
    }

    public void parseAS(InputStream asIn, String sourceId) throws ParseException, JasonException {
        as2j parser = new as2j(asIn);
        parser.setASLSource(sourceId);
        parser.agent(this);
    }
    public void parseAS(Reader asIn, String sourceId) throws ParseException, JasonException {
        as2j parser = new as2j(asIn);
        parser.setASLSource(sourceId);
        parser.agent(this);
    }

    @SuppressWarnings("unchecked")
    public InternalAction getIA(String iaName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
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
                objIA = (InternalAction)iaclass.getConstructor().newInstance();
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
            functions = new HashMap<>();
        addFunction(Count.class, false); // the  Count function depends on the agent class (its BB)
    }

    /** register an arithmetic function implemented in Java */
    public void addFunction(Class<? extends ArithFunction> c) {
        addFunction(c,true);
    }
    /** register an arithmetic function implemented in Java */
    private void addFunction(Class<? extends ArithFunction> c, boolean user) {
        try {
            ArithFunction af = c.getConstructor().newInstance();
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
    public void addInitialBelsInBB() throws JasonException {
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

    private void addInitBel(Literal b) throws JasonException {
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

    public Option selectOption(List<Option> options) throws NoOptionException {
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

    public ActionExec selectAction(Queue<ActionExec> actions) {
        // make sure the selected Action is removed from actList
        // (do not return suspended intentions)

        /* // old code, suspended is now considered in hasFA; no need to sync, it is done in TS
         * synchronized (actList) {
            Iterator<ActionExec> i = actList.iterator();
            while (i.hasNext()) {
                ActionExec a = i.next();
                if (!a.getIntention().isSuspended()) {
                    i.remove();
                    return a;
                }
            }
        }*/

        if (actions.isEmpty())
            return null;
        else
            return actions.poll();
    }

    /**
     * Gets relevant plans for a trigger event (teP), usually from a plan library.
     * A relevant plan is represented by an Option (a plan and a unifier).
     *
     * if evt is not null, that event is used as the context where teP was produced.
     * It can be used to get the proper plan library scope to retrieve plans.
     *
     */
    public List<Option> relevantPlans(Trigger teP, Event event) throws JasonException {
        Trigger te = teP.clone();
        List<Option> rp = null;

        // gets the proper plan library (root, inner scope, ...)
        PlanLibrary plib = getPL();
        if (event != null && event.isInternal() && !event.getIntention().isFinished()) {
            Plan p = event.getIntention().peek().getPlan();
            if (p.hasSubPlans()) {
                plib = p.getSubPlans();
            } else if (p.getScope() != null) {
                plib = p.getScope();
            }
        }


        while (plib != null) {
            List<Plan> candidateRPs = plib.getCandidatePlans(te);
            if (candidateRPs != null) {
                for (Plan pl : candidateRPs) {

                    Unifier relUn = null;
                    if (event != null && event.isInternal()) {
                        // use IM vars in the context for sub-plans (new in JasonER)
                        for (IntendedMeans im: event.getIntention()) {
                            if (im.getPlan().hasSubPlans() && im.getPlan().getSubPlans().get(pl.getLabel()) != null) {
                                relUn = im.triggerUnif.clone();
                                break;
                            }
                        }
                    }

                    relUn = pl.isRelevant(te, relUn);
                    if (relUn != null) {
                        if (rp == null) rp = new LinkedList<>();
                        rp.add(new Option(pl, relUn, event));
                    }
                }
            }
            plib = plib.getFather();
        }

        /* (previous to JasonER)
        List<Plan> candidateRPs = ag.pl.getCandidatePlans(te);
        if (candidateRPs != null) {
            for (Plan pl : candidateRPs) {
                Unifier relUn = pl.isRelevant(te, null);
                if (relUn != null) {
                    if (rp == null) rp = new LinkedList<>();
                    rp.add(new Option(pl, relUn));
                }
            }
        }*/
        return rp;
    }

    public List<Option> applicablePlans(List<Option> rp) throws JasonException {
        getTS().getC().syncApPlanSense.lock();
        try {
            List<Option> ap = null;
            if (rp != null) {
                for (Option opt: rp) {
                    LogicalFormula context = opt.getPlan().getContext();
                    if (getLogger().isLoggable(Level.FINE))
                        getLogger().log(Level.FINE, "option for "+getTS().getC().SE.getTrigger()+" is plan "+opt.getPlan().getLabel() + " " + opt.getPlan().getTrigger() + " : " + context + " -- with unification "+opt.getUnifier());

                    if (context == null) { // context is true
                        if (ap == null) ap = new LinkedList<>();
                        ap.add(opt);
                        if (getLogger().isLoggable(Level.FINE))
                            getLogger().log(Level.FINE, "     "+opt.getPlan().getLabel() + " is applicable with unification "+opt.getUnifier());
                    } else {
                        boolean allUnifs = opt.getPlan().isAllUnifs();

                        Iterator<Unifier> r = context.logicalConsequence(this, opt.getUnifier());
                        boolean isApplicable = false;
                        if (r != null) {
                            while (r.hasNext()) {
                                isApplicable = true;
                                opt.setUnifier(r.next());

                                if (ap == null) ap = new LinkedList<>();
                                ap.add(opt);

                                if (getLogger().isLoggable(Level.FINE))
                                    getLogger().log(Level.FINE, "     "+opt.getPlan().getLabel() + " is applicable with unification "+opt.getUnifier());

                                if (!allUnifs) break; // returns only the first unification
                                if (r.hasNext()) {
                                    // create a new option for the next loop step
                                    opt = new Option(opt.getPlan(), null, opt.getEvt());
                                }
                            }
                        }

                        if (!isApplicable && getLogger().isLoggable(Level.FINE))
                            getLogger().log(Level.FINE, "     "+opt.getPlan().getLabel() + " is not applicable");
                    }
                }
            }
            return ap;
        } finally {
            getTS().getC().syncApPlanSense.unlock();
        }
    }


    /** TS Initialisation (called by the AgArch) */
    public void setTS(TransitionSystem ts) {
        this.ts = ts;
        setLogger(ts.getAgArch());
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
        // complexity: 2n + n*m (n = number of percepts; m = number of beliefs)

        HashSet percepts = clone from the list of current environment percepts // 1n

        for b in BBPercept (the set of perceptions already in BB) // 1n * m
            if b not in percepts // constant time test
                remove b in BBPercept // constant time
                remove b in percept   // linear time

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
        Set<StructureWrapperForLiteral> perW = new HashSet<>();
        Iterator<Literal> iper = percepts.iterator();
        while (iper.hasNext()) {
            Literal l = iper.next();
            if (l != null)
                perW.add(new StructureWrapperForLiteral(l));
        }


        // deleting percepts in the BB that are not perceived anymore
        Iterator<Literal> perceptsInBB = getBB().getPercepts();
        while (perceptsInBB.hasNext()) {
            Literal l = perceptsInBB.next();
            if (l.subjectToBUF() && ! perW.remove(new StructureWrapperForLiteral(l))) { // l is not perceived anymore
                dels++;
                perceptsInBB.remove(); // remove l as perception from BB

                // new version (it is certain that l is in BB, only clone l when the event is relevant)
                Trigger te = new Trigger(TEOperator.del, TEType.belief, l);
                if (ts.getC().hasListener() || pl.hasCandidatePlan(te)) {
                    l = ASSyntax.createLiteral(l.getFunctor(), l.getTermsArray());
                    l.addAnnot(BeliefBase.TPercept);
                    te.setLiteral(l);
                    ts.getC().addEvent(new Event(te));
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
                // new version (it is certain that l is in BB, only clone l when the event is relevant)
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
                    ts.updateEvents(new Event(new Trigger(TEOperator.add, TEType.belief, lp)));
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
        bb.getLock().lock();
        try {
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
        } finally {
            bb.getLock().unlock();
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
        // It simply adds/dels the belief.

        int position = 0; // add in the begining
        if (addEnd)
            position = 1;

        List<Literal>[] result = null;
        bb.getLock().lock();
        try {
            try {
                if (beliefToAdd != null) {
                    if (logger.isLoggable(Level.FINE)) logger.fine("Doing (add) brf for " + beliefToAdd);

                    if (getBB().add(position, beliefToAdd)) {
                        result = new List[2];
                        result[0] = Collections.singletonList(beliefToAdd);
                        result[1] = Collections.emptyList();
                        if (logger.isLoggable(Level.FINE)) logger.fine("brf added " + beliefToAdd);
                    }
                }

                if (beliefToDel != null) {
                    Unifier u;
                    try {
                        u = i.peek().unif; // get from current intention
                    } catch (Exception e) {
                        u = new Unifier();
                    }

                    if (logger.isLoggable(Level.FINE)) logger.fine("Doing (del) brf for " + beliefToDel + " in BB=" + believes(beliefToDel, u));

                    boolean removed = getBB().remove(beliefToDel);
                    if (!removed && !beliefToDel.isGround()) { // then try to unify the parameter with a belief in BB
                        Iterator<Literal> il = getBB().getCandidateBeliefs(beliefToDel.getPredicateIndicator());
                        if (il != null) {
                            while (il.hasNext()) {
                                Literal linBB = il.next();
                                if (u.unifies(beliefToDel, linBB)) {
                                    beliefToDel = (Literal)beliefToDel.capply(u);
                                    linBB.delAnnots(beliefToDel.getAnnots());
                                    if (!linBB.hasSource()) {
                                        il.remove();
                                        removed = true;
                                    }
                                    break;
                                }
                            }
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
        } finally {
            bb.getLock().unlock();
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
        List<Literal> toDel = new ArrayList<>();
        if (un == null) un = new Unifier();
        bb.getLock().lock();
        try {
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
        } finally {
            bb.getLock().unlock();
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


    private Lock agStateLock = new ReentrantLock();
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
        Document docDOM = builder.newDocument();
        docDOM.appendChild(docDOM.createProcessingInstruction("xml-stylesheet", "href='http://jason.sf.net/xml/agInspection.xsl' type='text/xsl' "));

        agStateLock.lock();
        try {
            Element ag = getAsDOM(docDOM);
            docDOM.appendChild(ag);

            ag.appendChild(ts.getC().getAsDOM(docDOM));
            return docDOM;
        } finally {
            agStateLock.unlock();
        }
    }

    @Override
    public String toString() {
        return "Agent from "+getASLSrc();
    }

    /** Gets the agent "mind" as XML */
    public Element getAsDOM(Document document) {
        Element ag = document.createElement("agent");
        ag.setAttribute("name", ts.getAgArch().getAgName());
        ag.setAttribute("cycle", ""+ts.getAgArch().getCycleNumber());

        Node importedNodeBB = document.importNode(bb.getAsDOM(document), true);
        ag.appendChild(importedNodeBB);
        Node importedNodePL = document.importNode(getPL().getAsDOM(document), true);
        ag.appendChild(importedNodePL);

        // agent status
        Element ess = document.createElement("status");
        ag.appendChild(ess);
        Map<String,Object> status = getTS().getAgArch().getStatus();
        for (String k: status.keySet()) {
            Element es = document.createElement("entry");
            es.setAttribute("key", k);
            es.setAttribute("value", status.get(k).toString());
            ess.appendChild(es);
        }
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
        Element ag = document.createElement("agent");
        if (getASLSrc() != null && getASLSrc().length() > 0) {
            ag.setAttribute("source", getASLSrc());
        }
        ag.appendChild(bb.getAsDOM(document));
        ag.appendChild(pl.getAsDOM(document));
        document.appendChild(ag);

        return document;
    }

    /** parse and load the agent code, asSrc may be null
     * @deprecated use initAg() and load(src)
     */
    @Deprecated
    public void initAg(String asSrc) throws Exception {
        initAg();
        loadInitialAS(asSrc);
    }

    /* @deprecated use loadInitialASL */
    @Deprecated
    public void load(String asSrc) throws Exception {
        loadInitialAS(asSrc);
    }

    /* @deprecated use loadASL */
    @Deprecated
    public void loadAgSrc(String asSrc) throws Exception {
        loadAS(asSrc);
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
    @Deprecated
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
}
