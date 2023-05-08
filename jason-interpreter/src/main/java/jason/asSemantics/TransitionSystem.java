package jason.asSemantics;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.NoValueException;
import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSemantics.GoalListener.GoalStates;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.BinaryStructure;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.PlanBodyImpl;
import jason.pl.PlanLibrary;
import jason.asSyntax.SourceInfo;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.runtime.Settings;
import jason.stdlib.add_nested_source;
import jason.stdlib.desire;
import jason.stdlib.fail_goal;
import jason.stdlib.succeed_goal;
import jason.util.Config;
import jason.util.RunnableSerializable;


public class TransitionSystem implements Serializable {

    @Serial
    private static final long serialVersionUID = -5166620620196199391L;

    public enum State { StartRC, SelEv, RelPl, ApplPl, SelAppl, FindOp, AddIM, ProcAct, SelInt, ExecInt, ClrInt }

    private transient Logger logger  = null;

    private Agent         ag;
    private AgArch        agArch;
    private Circumstance  C;
    private Settings      setts;
    //private State         step       = State.StartRC; // first step of the SOS

    private State         stepSense       = State.StartRC;
    private State         stepDeliberate  = State.SelEv;
    private State         stepAct         = State.ProcAct;


    private int           nrcslbr         = Settings.ODefaultNRC; // number of reasoning cycles since last belief revision

    private boolean       sleepingEvt     = false;

    private List<GoalListener>  goalListeners = null;

    private Queue<RunnableSerializable> taskForBeginOfCycle = new ConcurrentLinkedQueue<>();

    public TransitionSystem(Agent a, Circumstance c, Settings s, AgArch ar) {
        ag     = a;
        agArch = ar;

        if (s == null)
            setts = new Settings();
        else
            setts = s;

        if (c == null)
            C = new Circumstance();
        else
            C = c;
        C.setTS(this);

        nrcslbr = setts.nrcbp(); // to do BR to start with

        setLogger(agArch);
        if (setts != null && setts.verbose() >= 0)
            logger.setLevel(setts.logLevel());

        if (a != null)
            a.setTS(this);

        if (ar != null)
            ar.setTS(this);
    }

    public void setLogger(AgArch arch) {
        if (arch != null)
            logger = Logger.getLogger(TransitionSystem.class.getName() + "." + arch.getAgName());
        else
            logger = Logger.getLogger(TransitionSystem.class.getName());
    }
    public void setLogger(Logger l) {
        logger = l;
    }

    // ---------------------------------------------------------
    //    Goal Listeners support methods

    private Map<GoalListener,CircumstanceListener> listenersMap; // map the circumstance listeners created for the goal listeners, used in remove goal listener

    /** adds an object that will be notified about events on goals (creation, suspension, ...) */
    public void addGoalListener(final GoalListener gl) {
        if (goalListeners == null) {
            goalListeners = new ArrayList<>();
            listenersMap  = new HashMap<>();
        } else {
            // do not install two MetaEventGoalListener
            for (GoalListener g: goalListeners)
                if (g instanceof GoalListenerForMetaEvents)
                    return;
        }

        // we need to add a listener in C to map intention events to goal events
        CircumstanceListener cl = new CircumstanceListener() {
            private static final long serialVersionUID = 1L;

            @Override public void intentionDropped(Intention i) {
                for (IntendedMeans im: i)
                    if (im.getTrigger().isAddition() && im.getTrigger().isGoal())
                        gl.goalFinished(im.getTrigger(), GoalStates.dropped);
            }

            @Override public void intentionSuspended(Trigger t, Intention i, Term reason) {
                if (t.isAddition() && t.isGoal())
                    gl.goalSuspended(t, reason);
            }
            @Override public void intentionWaiting(Intention i, Term reason) {
                //for (IntendedMeans im: i)
                IntendedMeans im = i.peek();
                if (im.getTrigger().isAddition() && im.getTrigger().isGoal())
                    gl.goalWaiting(im.getTrigger(), reason);
            }
            @Override public void intentionResumed(Intention i, Term reason) {
                IntendedMeans im = i.peek();
                if (im.getTrigger().isAddition() && im.getTrigger().isGoal())
                    gl.goalResumed(im.getTrigger(), reason);
            }

            @Override public void intentionExecuting(Intention i, Term reason) {
                IntendedMeans im = i.peek();
                if (im.getTrigger().isAddition() && im.getTrigger().isGoal())
                    gl.goalExecuting(im.getTrigger(), reason);
            }

            @Override public void eventAdded(Event e) {
                if (e.getTrigger().isAddition() && e.getTrigger().isGoal())
                    gl.goalStarted(e);
            }
        };
        C.addEventListener(cl);
        listenersMap.put(gl,cl);

        goalListeners.add(gl);
    }

    public boolean hasGoalListener() {
        return goalListeners != null && !goalListeners.isEmpty();
    }

    public List<GoalListener> getGoalListeners() {
        return goalListeners;
    }

    public boolean removeGoalListener(GoalListener gl) {
        CircumstanceListener cl = (listenersMap == null ? null : listenersMap.get(gl));
        if (cl != null)
            C.removeEventListener(cl);
        return goalListeners.remove(gl);
    }

    /** ******************************************************************* */
    /* SEMANTIC RULES */
    /** ******************************************************************* */

    private void applySemanticRuleSense() throws JasonException {
        switch (stepSense) {
        case StartRC:
            applyProcMsg();
            break;
        default:
            break;
        }
    }

    private void applySemanticRuleDeliberate() throws JasonException {
        switch (stepDeliberate) {
        case SelEv:
            applySelEv();
            break;
        case RelPl:
            applyRelPl();
            break;
        case ApplPl:
            applyApplPl();
            break;
        case SelAppl:
            applySelAppl();
            break;
        case FindOp:
            applyFindOp();
            break;
        case AddIM:
            applyAddIM();
            break;
        default:
            break;
        }
    }

    private void applySemanticRuleAct() throws JasonException {
        switch (stepAct) {
        case ProcAct:
            applyProcAct();
            break;
        case SelInt:
            applySelInt();
            break;
        case ExecInt:
            applyExecInt();
            break;
        case ClrInt:
            stepAct = State.StartRC;
            applyClrInt(C.SI);
            break;
        default:
            break;
        }
    }

    /*
    private void applySemanticRule() throws JasonException {
        // check the current step in the reasoning cycle
        // only the main parts of the interpretation appear here
        // the individual semantic rules appear below

        switch (step) {
        case StartRC:   applyProcMsg(); break;
        case SelEv:     applySelEv(); break;
        case RelPl:     applyRelPl();  break;
        case ApplPl:    applyApplPl(); break;
        case SelAppl:   applySelAppl(); break;
        case FindOp:    applyFindOp(); break;
        case AddIM:     applyAddIM(); break;
        case ProcAct:   applyProcAct(); break;
        case SelInt:    applySelInt(); break;
        case ExecInt:   applyExecInt(); break;
        case ClrInt:    step = State.StartRC;
                        applyClrInt(C.SI);
                        break;
        }
    }
    */

    // the semantic rules are referred to in comments in the functions below

    private final String kqmlReceivedFunctor = Config.get().getKqmlFunctor();

    private void applyProcMsg() throws JasonException {
        stepSense = State.SelEv;
        if (C.hasMsg()) {
            Message m = ag.selectMessage(C.getMailBox());
            if (m == null) return;

            // get the content, it can be any term (literal, list, number, ...; see ask)
            Term content = null;
            if (m.getPropCont() instanceof Term) {
                content = (Term)m.getPropCont();
            } else {
                try {
                    content = ASSyntax.parseTerm(m.getPropCont().toString());
                } catch (ParseException e) {
                    //logger.warning("The content of the message '"+m.getPropCont()+"' is not a term! Using ObjectTerm.");
                    content = new ObjectTermImpl(m.getPropCont());
                    //return;
                }
            }

            // check if an intention was suspended waiting this message
            Intention intention = null;
            if (m.getInReplyTo() != null) {
                intention = getC().getPendingIntentions().get(m.getInReplyTo());
            }
            // is it a pending intention?
            if (intention != null) {
                // unify the message answer with the .send fourth argument.
                // the .send that put the intention in Pending state was
                // something like
                //    .send(ag1,askOne, value, X)
                // if the answer was tell 3, unifies X=3
                // if the answer was untell 3, unifies X=false
                Structure send = (Structure)intention.peek().getCurrentStep().getBodyTerm();
                if (m.isUnTell() && send.getTerm(1).toString().equals("askOne")) {
                    content = Literal.LFalse;
                } else if (content.isLiteral()) { // adds source in the content if possible
                    content = add_nested_source.addAnnotToList(content, new Atom(m.getSender()));
                } else if (send.getTerm(1).toString().equals("askAll") && content.isList()) { // adds source in each answer if possible
                    ListTerm tail = new ListTermImpl();
                    for (Term t: ((ListTerm)content)) {
                        t = add_nested_source.addAnnotToList(t, new Atom(m.getSender()));
                        tail.append(t);
                    }
                    content = tail;
                } else if (send.getTerm(1).toString().equals("askHow") && content.isList()) {  // adds source in each answer if possible
                    ListTerm tail = new ListTermImpl();
                    for (Term t: ((ListTerm)content)) {
                        if (t instanceof Plan p) {
                            p.addSource(new Atom(m.getSender()));
                            tail.append(p);
                        }
                    }
                    content = tail;
                }

                // test the case of sync ask with many receivers
                Unifier un = intention.peek().getUnif();
                Term rec = send.getTerm(0).capply(un);
                if (rec.isList()) { // send to many receivers
                    // put the answers in the unifier
                    VarTerm answers = new VarTerm("AnsList___"+m.getInReplyTo());
                    ListTerm listOfAnswers = (ListTerm)un.get(answers);
                    if (listOfAnswers == null) {
                        listOfAnswers = new ListTermImpl();
                        un.unifies(answers, listOfAnswers);
                    }
                    listOfAnswers.append(content);
                    int nbReceivers = ((ListTerm)rec).size();
                    if (listOfAnswers.size() == nbReceivers) { // all agents have answered
                        resumeSyncAskIntention(m.getInReplyTo(), send.getTerm(3), listOfAnswers);
                    }
                } else {
                    resumeSyncAskIntention(m.getInReplyTo(), send.getTerm(3), content);
                }

                // the message is not an ask answer
            } else if (ag.socAcc(m)) {

                if (! m.isReplyToSyncAsk()) { // ignore answer after the timeout
                    // generate an event
                    String sender = m.getSender();
                    if (sender.equals(getAgArch().getAgName()))
                        sender = "self";

                    boolean added = false;
                    if (!setts.isSync() && !ag.getPL().hasUserKqmlReceivedPlans() && content.isLiteral() && !content.isList()) { // optimisation to jump kqmlPlans
                        if (m.getIlForce().equals("achieve") ) {
                            content = add_nested_source.addAnnotToList(content, new Atom(sender));
                            C.addEvent(new Event(new Trigger(TEOperator.add, TEType.achieve, (Literal)content), Intention.EmptyInt));
                            added = true;
                        } else if (m.getIlForce().equals("tell") ) {
                            content = add_nested_source.addAnnotToList(content, new Atom(sender));
                            getAg().addBel((Literal)content);
                            added = true;
                        } else if (m.getIlForce().equals("signal") ) {
                            content = add_nested_source.addAnnotToList(content, new Atom(sender));
                            ((Literal)content).addAnnot(new Atom("signal"));
                            C.addEvent(new Event(new Trigger(TEOperator.add, TEType.signal, (Literal)content), Intention.EmptyInt));
                            added = true;
                        }
                    }

                    if (!added) {
                        Literal received = new LiteralImpl(kqmlReceivedFunctor).addTerms(
                            new Atom(sender),
                            ASSyntax.createAtom(m.getIlForce()),
                            content,
                            new Atom(m.getMsgId()));

                        updateEvents(new Event(new Trigger(TEOperator.add, TEType.achieve, received), Intention.EmptyInt));
                    }
                } else {
                    logger.fine("Ignoring message "+m+" because it is received after the timeout.");
                }
            }
        }
    }

    private void resumeSyncAskIntention(String msgId, Term answerVar, Term answerValue) throws JasonException {
        Intention i = getC().removePendingIntention(msgId);
        i.peek().removeCurrentStep(); // removes the .send in the plan body
        if (i.peek().getUnif().unifies(answerVar, answerValue)) {
            getC().resumeIntention(i, ASSyntax.createStructure("ask_answer", new StringTermImpl(msgId)));
        } else {
            generateGoalDeletion(i, JasonException.createBasicErrorAnnots("ask_failed", "reply of an ask message ('"+answerValue+"') does not unify with fourth argument of .send ('"+answerVar+"')"), ASSyntax.createAtom("ask_failed"));
        }

    }

    private static Plan planBaseForEPlans = null;

    private void applySelEv() throws JasonException {
        // Rule for atomic, if there is an atomic intention, do not select event
        if (C.hasAtomicIntention()) {
            stepDeliberate = State.ProcAct; // need to go to ProcAct to see if an atomic intention received a feedback action
            return;
        }

        // Rule for atomic, events from atomic intention have priority
        C.SE = C.removeAtomicEvent();
        if (C.SE != null) {
            stepDeliberate = State.RelPl;
            return;
        }

        if (C.hasEvent()) {
            // Rule SelEv1
            C.SE = ag.selectEvent(C.getEvents());
            if (logger.isLoggable(Level.FINE))
                logger.fine("Selected event "+C.SE);
            if (C.SE != null) {

                // update (external) events (+ and -) are copied for all intentions interested on them (new JasonER)
                // possibly creating branches for these intentions
                // an intention is interested in the event if some of its IM has sub-plans for it.
                //
                // pseudocode:
                //     for all intentions i interested in the external event
                //         create a clone intention of i to handle the event
                //
                if (C.SE.isExternal() && C.SE.getTrigger().isUpdate()) {
                    //logger.info("Selected event "+C.SE.getTrigger()+  C.SE.isExternal());
                    Iterator<Intention> ii = C.getAllIntentions();
                    while (ii.hasNext()) {
                        Intention i = ii.next();
                        //logger.info("**"+C.SE + "-- "+i.getId()+" "+i.hasInterestInUpdateEvents()+" "+ (C.SE.getIntention() != null ? C.SE.getIntention().getId() : " no int "));

                        // if intention i has sub plans (so potentially interested in external events)
                        if (i.hasInterestInUpdateEvents()) {
                            // consider all goals in the intention stack that have sub-plans
                            outerloop:
                            for (IntendedMeans im: i) {
                                if (im.getPlan().hasSubPlans()) {
                                    List<Plan> relPlans = im.getPlan().getSubPlans().getCandidatePlans(C.SE.getTrigger());
                                    //System.out.println("***"+C.SE.getTrigger()+" try plans in "+im.getPlan().getTrigger()+" -- "+(relPlans != null));
                                    if (relPlans != null) {
                                        for (Plan p: relPlans) {
                                            // test if we have an option
                                            Option o = getOption(C.SE, p, im.getUnif().clone());
                                            //logger.info("option "+o+" "+C.SE.getTrigger()+" for "+p+" with "+i.peek().getUnif());
                                            if (o != null) {
                                                // create the extra event based on intention i (to mimic the branching)
                                                // and move intention i from I to E
                                                try {
                                                    if (planBaseForEPlans == null)
                                                        planBaseForEPlans = ASSyntax.parsePlan("+artificial_plan <- .print(dropwhenreturntohere); .drop_intention."); // TODO: do not use drop_intention, but a new internal action that drops only this intention (important if the user decides to use .drop_intention to stop the g-plan)

                                                    IntendedMeans joinIM = new IntendedMeans(new Option(planBaseForEPlans.cloneNS(Literal.DefaultNS), i.peek().getUnif()), C.SE.getTrigger());
                                                    Intention newi = new Intention();
                                                    newi.setGIntention(i); // to avoid succeed_goal to resume it
                                                    i.copyTo(newi);
                                                    newi.setNoInterestInUpdateEvents();
                                                    newi.push(joinIM);

                                                    Event e = new Event(C.SE.getTrigger(), newi);
                                                    //logger.info("     ** add extra evt for "+e.getTrigger()+" ");
                                                    e.setOption(o);
                                                    C.addEvent(e);
                                                } catch (Exception e1) {
                                                    e1.printStackTrace();
                                                }

                                                break outerloop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (ag.hasCustomSelectOption() || setts.verbose() == 2) // verbose == 2 means debug mode
                    stepDeliberate = State.RelPl;
                else
                    stepDeliberate = State.FindOp;
            }
        } else {
            // Rule SelEv2
            // directly to ProcAct if no event to handle
            stepDeliberate = State.ProcAct;
        }
    }

    // internal action used to handle goal condition (new in JasonER)
    private succeed_goal scia = new succeed_goal();
    int nbOfGoalConditions = 0;
    private IMCondition  imcondSat = new IMCondition() {
        public boolean test(Trigger t, Unifier u) {
            return false;
        }
        public Trigger getTrigger() {
            return null;
        }
        public boolean test(IntendedMeans im, Unifier u) {
            if (im.getPlan().hasGoalCondition() && !im.getPlan().getGoalCondition().equals(Literal.LFalse)) { // no need to test goal condition = "false"
                nbOfGoalConditions++;
            }
            return im.isSatisfied(getAg());
        }
    };

    private void applyClrSatInt() {
        // remove all intentions with GoalCondition satisfied (new JasonER)
        if (C.hasIntentionWithGoalCondition()) {
            nbOfGoalConditions = 0;
            try {
                scia.drop(this, imcondSat, new Unifier());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (nbOfGoalConditions == 0) { // if no intention with GC anymore, stop checking
                C.resetIntentionsWithGoalCondition();
            }
        }
    }

    private void applyRelPl() throws JasonException {
        if (C.SE.getOption() == null) { // no option yet
            // get all relevant plans for the selected event
            C.RP = relevantPlans(C.SE.trigger, C.SE);

            // Rule Rel1
            if (C.RP != null || setts.retrieve())
                // retrieve is mainly for Coo-AgentSpeak
                stepDeliberate = State.ApplPl;
            else
                applyRelApplPlRule2("relevant");
        } else {
            stepDeliberate = State.ApplPl;
        }
    }

    private void applyApplPl() throws JasonException {
        if (C.SE.getOption() == null) { // no option yet
            C.AP = applicablePlans(C.RP);

            // Rule Appl1
            if (C.AP != null || setts.retrieve())
                // retrieve is mainly for Coo-AgentSpeak
                stepDeliberate = State.SelAppl;
            else
                applyRelApplPlRule2("applicable");
        } else {
            stepDeliberate = State.SelAppl;
        }
    }

    /** generates goal deletion event */
    private void applyRelApplPlRule2(String m) throws JasonException {
        stepDeliberate = State.ProcAct; // default next step
        if (C.SE.trigger.isGoal() && !C.SE.trigger.isMetaEvent()) {
            // can't carry on, no relevant/applicable plan.
            try {
                if (C.SE.getIntention() != null && C.SE.getIntention().size() > 3000) {
                    logger.warning("we are likely in a problem with event "+C.SE.getTrigger()+" the intention stack has already "+C.SE.getIntention().size()+" intended means!");
                }
                String msg = "Found a goal for which there is no "+m+" plan: " + C.SE.getTrigger();
                if (!generateGoalDeletionFromEvent(JasonException.createBasicErrorAnnots("no_"+m, msg), ASSyntax.createAtom("no_"+m+"_plan"))) {
                    logger.warning(msg);

                    // show details of evaluation of each candidate plan
                    if (C.RP != null) {
                        Level oldLevel = ag.getLogger().getLevel();
                        ag.getLogger().setLevel(Level.FINE);
                        for (Option o: C.RP) {
                            if (o.getUnifier() == null) {
                                ag.getLogger().fine("Plan @"+o.getPlan().getLabel()+" "+o.getPlan().getTrigger());
                                ag.getLogger().fine("     |> not relevant");
                            } else {
                                ag.getLogger().fine("Plan @"+o.getPlan().getLabel()+" "+o.getPlan().getTrigger() + " : " + o.getPlan().getContext()+ " -- "+ o.getUnifier());
                                LogicalFormula context = o.getPlan().getContext();
                                Iterator<Unifier> r = context.logicalConsequence(ag, o.getUnifier());
                                while (r != null && r.hasNext()) {
                                    r.next(); // just to evaluate and show debug messages
                                }
                            }
                        }
                        ag.getLogger().setLevel(oldLevel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        } else if (C.SE.isInternal()) {
            // e.g. belief addition as internal event, just go ahead
            // but note that the event was relevant, yet it is possible
            // the programmer just wanted to add the belief and it was
            // relevant by chance, so just carry on instead of dropping the
            // intention
            Intention i = C.SE.intention;
            joinRenamedVarsIntoIntentionUnifier(i.peek(), i.peek().unif);
            removeActionReQueue(i);
        } else if (setts.requeue()) {
            // if external, then needs to check settings
            C.addEvent(C.SE);
        } else {
            // current event is external and irrelevant,
            // discard that event and select another one
            stepDeliberate = State.SelEv;
        }
    }


    private void applySelAppl() throws JasonException {
        // Rule SelAppl
        if (C.SE.getOption() == null) { // no option yet (an option could be set by selEvt JasonER part)
            C.SO = ag.selectOption(C.AP);
        } else {
            C.SO = C.SE.getOption();
        }

        if (C.SO != null) {
            stepDeliberate = State.AddIM;
            if (logger.isLoggable(Level.FINE)) logger.fine("Selected option "+C.SO+" for event "+C.SE);
        } else {
            logger.fine("** selectOption returned null!");
            generateGoalDeletionFromEvent(JasonException.createBasicErrorAnnots("no_option", "selectOption returned null"), ASSyntax.createAtom("no_option"));
            // can't carry on, no applicable plan.
            stepDeliberate = State.ProcAct;
        }
    }

    /**
     * This step is new in Jason 1.1 and replaces the steps RelPl->ApplPl->SelAppl when the user
     * does not customise selectOption. This version does not create the RP and AP lists and thus
     * optimise the reasoning cycle. It searches for the first option and automatically selects it.
     *
     * @since 1.1
     */
    private void applyFindOp() throws JasonException {
        stepDeliberate = State.AddIM; // default next step

        // consider scope (new in JasonER)
        // TODO: implement Scope for RelPl ApplPl SelAppl (besides the fast track of applyFindOp)
        C.SO = C.SE.getOption(); // set by selEvt (JasonER part)
        if (C.SO != null) { // an option was previously computed
            return;
        }

        // gets the proper plan library (root, inner scope, ...)
        PlanLibrary plib = ag.getPL();
        if (C.SE.isInternal() && !C.SE.getIntention().isFinished()) {
            Plan p = C.SE.getIntention().peek().getPlan();
            if (p.hasSubPlans()) {
                plib = p.getSubPlans();
            } else if (p.getScope() != null) {
                plib = p.getScope();
            }
        }

        String kindOfError = "relevant";
        while (plib != null) {
            // get all relevant plans for the selected event
            // TODO: compute as in 2.6.2 // C.RP = new ArrayList<>();
            List<Plan> candidateRPs = plib.getCandidatePlans(C.SE.getTrigger());
            if (candidateRPs != null) {
                kindOfError = "applicable";
                for (Plan pl : candidateRPs) {
                    C.SO = getOption(C.SE, pl, null);
                    if (C.SO != null)
                        return;
                }
            }
            plib = plib.getFather();
        }

        applyRelApplPlRule2(kindOfError);
    }

    private Option getOption(Event evt, Plan pl, Unifier relUn) {
        if (evt.isInternal()) {
            // use IM vars in the context for sub-plans (new in JasonER)
            for (IntendedMeans im: evt.getIntention()) {
                if (im.getPlan().hasSubPlans() && im.getPlan().getSubPlans().get(pl.getLabel()) != null) {
                    relUn = im.triggerUnif.clone();
                    break;
                }
            }
        }

        relUn = pl.isRelevant(evt.trigger, relUn);
        if (relUn != null) { // is relevant
            LogicalFormula context = pl.getContext();
            if (context == null) { // context is true
                return new Option(pl, relUn);
            } else {
                Iterator<Unifier> r = context.logicalConsequence(ag, relUn);
                if (r != null && r.hasNext()) {
                    return new Option(pl, r.next());
                }
            }
        }
        return null;
    }

    private void applyAddIM() throws JasonException {
        // create a new intended means
        IntendedMeans im = new IntendedMeans(C.SO, C.SE.getTrigger());

        if (C.SE.getTrigger().isAchvGoal() && C.SE.getTrigger().isAddition()) {
            if (hasGoalListener())
                for (GoalListener gl: getGoalListeners())
                    gl.goalExecuting(C.SE.getTrigger(), null);
        }

        // Rule ExtEv
        if (C.SE.intention == Intention.EmptyInt) {
            Intention intention = new Intention();
            intention.push(im);
            C.addRunningIntention(intention);
        } else {
            // Rule IntEv

            // begin tail recursion optimisation (TRO)
            if (setts.isTROon()) {
                IntendedMeans top = C.SE.intention.peek(); // top = the IM that will be removed from the intention due to TRO
                //System.out.println(top.getTrigger().isGoal()+"=1="+im.getTrigger().isGoal());
                //System.out.println(top.getTrigger().getLiteral().getPredicateIndicator()+"=2="+im.getTrigger().getLiteral().getPredicateIndicator());
                //System.out.println(top.getTrigger()+"=3="+im.getTrigger());

                // next test if the condition for TOR (comparing top and the IM being added)
                if (top != null &&
                        top.getTrigger().isAddition() && im.getTrigger().isAddition() && // failure plans should not be subject of TRO (see BugFail)
                        top.getTrigger().isGoal() && im.getTrigger().isGoal() && // are both goal
                        top.getCurrentStep().getBodyNext() == null && // the plan below is finished
                        top.getTrigger().getLiteral().getPredicateIndicator().equals( im.getTrigger().getLiteral().getPredicateIndicator()) // goals are equals (do not consider - or + from the trigger -- required in the case of goal patterns where -!g <- !g is used)
                   ) {

                    if (hasGoalListener())
                        for (GoalListener gl: getGoalListeners())
                            gl.goalFinished(top.getTrigger(), GoalStates.achieved);

                    C.SE.intention.pop(); // remove the top IM

                    IntendedMeans imBase = C.SE.intention.peek(); // base = where the new IM will be placed on top of
                    if (imBase != null && imBase.renamedVars != null) {
                        // move top relevant values into the base (relevant = renamed vars in base)

                        for (VarTerm v: imBase.renamedVars) {
                            VarTerm vvl = (VarTerm)imBase.renamedVars.function.get(v);
                            Term t = top.unif.get(vvl);
                            if (t != null) { // if v has got a value in top unif, put the value in the unifier
                                if (t instanceof Literal) {
                                    Literal l = (Literal)t.capply(top.unif);
                                    l.makeVarsAnnon(top.renamedVars);
                                    im.unif.function.put(vvl, l);
                                } else {
                                    im.unif.function.put(vvl, t);
                                }
                            } else {
                                // the vvl was renamed again in top, just replace the new value in base
                                VarTerm v0 = (VarTerm)top.renamedVars.function.get(vvl);
                                if (v0 != null) {
                                    imBase.renamedVars.function.put(v, v0);
                                }
                            }
                        }
                    }
                }
            }
            // end of TRO

            C.SE.intention.push(im);
            C.addRunningIntention(C.SE.intention);
        }
        stepDeliberate = State.ProcAct;
    }

    private void applyProcAct() throws JasonException {
        applyClrSatInt(); // TODO: create a proper step for ClrSatInt in the enumerations

        stepAct = State.SelInt; // default next step
        if (C.hasFeedbackAction()) { // suspended intentions are not considered
            ActionExec a = null;
            synchronized (C.getFeedbackActions()) {
                a = ag.selectAction(C.getFeedbackActions());
            }
            if (a != null) {
                final Intention curInt = a.getIntention();

                // remove the intention from PA (PA has all pending action, including those in FA;
                // but, if the intention is not in PA, it means that the intention was dropped
                // and should not return to I)
                if (C.removePendingAction(curInt.getId()) != null) {
                    if (a.getResult()) {
                        // add the intention back in I
                        removeActionReQueue(curInt);
                        applyClrInt(curInt);

                        if (hasGoalListener())
                            for (GoalListener gl: getGoalListeners())
                                for (IntendedMeans im: curInt) //.getIMs())
                                    gl.goalExecuting(im.getTrigger(), ASSyntax.createStructure("action_executed", a.getActionTerm()));
                    } else {
                        String reason = a.getFailureMsg();
                        if (reason == null) reason = "";
                        ListTerm annots = JasonException.createBasicErrorAnnots("action_failed", reason);
                        if (a.getFailureReason() != null)
                            annots.append(a.getFailureReason());
                        generateGoalDeletion(curInt, annots, ASSyntax.createAtom("action_failed"));
                        C.removeAtomicIntention(); // if (potential) atomic intention is not removed, it will be selected in selInt or selEv and runs again
                    }
                } else {
                    applyProcAct(); // get next action
                }
            }
        }
    }

    private void applySelInt() throws JasonException {
        stepAct = State.ExecInt; // default next step

        // Rule for Atomic Intentions
        C.SI = C.removeAtomicIntention();
        if (C.SI != null) {
            return;
        }

        // Rule SelInt1
        if (!C.isAtomicIntentionSuspended() && C.hasRunningIntention()) { // the isAtomicIntentionSuspended is necessary because the atomic intention may be suspended (the above removeAtomicInt returns null in that case)
            // but no other intention could be selected
            C.SI = ag.selectIntention(C.getRunningIntentions());
            if (logger.isLoggable(Level.FINE)) logger.fine("Selected intention "+C.SI);
            if (C.SI != null) { // the selectIntention function returned null
                return;
            }
        }

        stepAct = State.StartRC;
    }

    @SuppressWarnings("unchecked")
    private void applyExecInt() throws JasonException {
        stepAct = State.ClrInt; // default next step

        final Intention curInt = C.SI;
        if (curInt == null)
            return;

        if (curInt.isFinished())
            return;

        // get next formula in the body of the intended means
        // on the top of the selected intention

        IntendedMeans im = curInt.peek();

        if (im.isFinished()) {
            // for empty plans! may need unif, etc
            removeActionReQueue(curInt);
            return;
        }
        Unifier     u = im.unif;
        PlanBody    h = im.getCurrentStep();
        C.setLastDeed(h);

        Term bTerm = h.getBodyTerm();

        if (bTerm instanceof VarTerm) { // de-var bTerm
            bTerm = bTerm.capply(u);
            if (bTerm.isVar()) { // the case of !A with A not ground
                String msg = h.getSrcInfo()+": "+ "Variable '"+bTerm+"' must be ground.";
                if (!generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("body_var_without_value", msg), null))
                    logger.log(Level.SEVERE, msg);
                return;
            }
            if (bTerm.isPlanBody()) {
                if (h.getBodyType() != BodyType.action) { // the case of ...; A = { !g }; +g; ....
                    String msg = h.getSrcInfo()+": "+ "The operator '"+h.getBodyType()+"' is lost with the variable '"+bTerm+"' unified with a plan body. ";
                    if (!generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("body_var_with_op", msg), null))
                        logger.log(Level.SEVERE, msg);
                    return;
                }
            }
            // translate var into appropriate body
            if (bTerm.isInternalAction())
                h = new PlanBodyImpl(BodyType.internalAction, bTerm);

            if (! (bTerm instanceof Literal) && h.getBodyType() != BodyType.constraint && h.getBodyType() != BodyType.test) {
                // var is not literal, so can not be used for most of the deeds
                String msg = h.getSrcInfo()+": "+ "Variable '"+h.getBodyTerm()+"' must be a Literal and not '"+bTerm+"' to be used in '"+h+"'.";
                if (!generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("body_var_not_literal", msg), null))
                    logger.log(Level.SEVERE, msg);
                return;
            }
        }

        if (bTerm.isPlanBody()) {
            h = (PlanBody)bTerm;
            if (h.getPlanSize() > 1) {
                h = (PlanBody)bTerm.clone();
                h.add(im.getCurrentStep().getBodyNext());
                im.insertAsNextStep(h.getBodyNext());
            }
            bTerm = h.getBodyTerm();
        }

        Literal body = null;
        if (bTerm instanceof Literal)
            body = (Literal)bTerm;

        switch (h.getBodyType()) {

        case none:
            break;

        // Rule Action
        case action:
            body = (Literal)body.capply(u);
            C.A = new ActionExec(body, curInt);
            break;

        case internalAction:
            boolean ok = false;
            List<Term> errorAnnots = null;
            try {
                InternalAction ia = ((InternalActionLiteral)bTerm).getIA(ag);
                Term[] terms      = ia.prepareArguments(body, u); // clone and apply args
                Object oresult    = ia.execute(this, u, terms);
                if (oresult != null) {
                    ok = oresult instanceof Boolean && (Boolean)oresult;
                    if (!ok && oresult instanceof Iterator) { // ia result is an Iterator
                        Iterator<Unifier> iu = (Iterator<Unifier>)oresult;
                        if (iu.hasNext()) {
                            // change the unifier of the current IM to the first returned by the IA
                            im.unif = iu.next();
                            ok = true;
                        }
                    }
                    if (!ok) { // IA returned false
                        errorAnnots = JasonException.createBasicErrorAnnots("ia_failed", "");
                    }
                }

                if (ok && !ia.suspendIntention())
                    removeActionReQueue(curInt);
            } catch (NoValueException e) {
                // add not ground vars in the message
                String msg = e.getMessage() + " Ungrounded variables = [";
                String v = "";
                for (VarTerm var: body.getSingletonVars()) {
                    if (u.get(var) == null) {
                        msg += v+var;
                        v = ",";
                    }
                }
                msg += "].";
                e = new NoValueException(msg);
                errorAnnots = e.getErrorTerms();
                if (!generateGoalDeletion(curInt, errorAnnots, ASSyntax.createAtom("ia_failed")))
                    logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage());
                ok = true; // just to not generate the event again

            } catch (JasonException e) {
                errorAnnots = e.getErrorTerms();
                if (!generateGoalDeletion(curInt, errorAnnots, ASSyntax.createAtom("ia_failed")))
                    logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage());
                ok = true; // just to not generate the event again
            } catch (Exception e) {
                if (body == null)
                    logger.log(Level.SEVERE, "Selected an intention with null body in '"+h+"' and IM "+im, e);
                else
                    logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage(), e);
            } catch (Error e) {
                logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage(), e);
            }
            if (!ok)
                generateGoalDeletion(curInt, errorAnnots, ASSyntax.createAtom("ia_failed"));

            break;

        case constraint:
            Iterator<Unifier> iu = ((LogicalFormula)bTerm).logicalConsequence(ag, u);
            if (iu.hasNext()) {
                im.unif = iu.next();
                removeActionReQueue(curInt);
            } else {
                String msg = "Constraint "+h+" was not satisfied ("+h.getSrcInfo()+") un="+u;
                generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots(ASSyntax.createAtom("constraint_failed"), msg), ASSyntax.createAtom("constraint_failed"));
                logger.fine(msg);
            }
            break;

        // Rule Achieve
        case achieve:
            body = prepareBodyForEvent(body, u, curInt.peek());
            Event evt = C.addAchvGoal(body, curInt);
            stepAct = State.StartRC;
            checkHardDeadline(evt);
            break;

        // Rule Achieve as a New Focus (the !! operator)
        case achieveNF:
            body = prepareBodyForEvent(body, u, null);
            evt  = C.addAchvGoal(body, Intention.EmptyInt);
            checkHardDeadline(evt);
            removeActionReQueue(curInt);
            break;

        // Rule Test
        case test:
            LogicalFormula f = (LogicalFormula)bTerm;
            if (ag.believes(f, u)) {
                removeActionReQueue(curInt);
            } else {
                boolean fail = true;
                // generate event when using literal in the test (no events for log. expr. like ?(a & b))
                if (f.isLiteral() && !(f instanceof BinaryStructure)) {
                    body = prepareBodyForEvent(body, u, curInt.peek());
                    if (body.isLiteral()) { // in case body is a var with content that is not a literal (note the VarTerm pass in the instanceof Literal)
                        Trigger te = new Trigger(TEOperator.add, TEType.test, body);
                        evt = new Event(te, curInt);
                        if (ag.getPL().hasCandidatePlan(te)) {
                            if (logger.isLoggable(Level.FINE)) logger.fine("Test Goal '" + bTerm + "' failed as simple query. Generating internal event for it: "+te);
                            C.addEvent(evt);
                            stepAct = State.StartRC;
                            fail = false;
                        }
                    }
                }
                if (fail) {
                    if (logger.isLoggable(Level.FINE)) logger.fine("Test '"+bTerm+"' failed ("+h.getSrcInfo()+").");
                    generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("test_goal_failed", "Failed to test '"+bTerm+"'"), ASSyntax.createAtom("test_goal_failed"));
                }
            }
            break;


        case delAddBel:
            // -+a(1,X) ===> remove a(_,_), add a(1,X)
            // change all vars to anon vars to remove it
            Literal b2 = prepareBodyForEvent(body, u, curInt.peek());
            b2.makeTermsAnnon(); // do not change body (but b2), to not interfere in addBel
            // to delete, create events as external to avoid that
            // remove/add create two events for the same intention
            // (in future releases, creates two branches for this operator)

            try {
                List<Literal>[] result = ag.brf(null, b2, curInt); // the intention is not the new focus
                if (result != null) { // really delete something
                    // generate events
                    updateEvents(result,Intention.EmptyInt);
                }
            } catch (RevisionFailedException re) {
                generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("belief_revision_failed", "BRF failed for '"+body+"'"), ASSyntax.createAtom("belief_revision_failed"));
                break;
            }

        // add the belief, so no break;

        // Rule AddBel
        case addBel:
        case addBelBegin:
        case addBelEnd:
        case addBelNewFocus:
            // calculate focus
            Intention newfocus = Intention.EmptyInt;
            boolean isSameFocus = setts.sameFocus() && h.getBodyType() != BodyType.addBelNewFocus;
            if (isSameFocus) {
                newfocus = curInt;
                body = prepareBodyForEvent(body, u, newfocus.peek());
            } else {
                body = prepareBodyForEvent(body, u, null);
            }

            // call BRF
            try {
                List<Literal>[] result;
                if (h.getBodyType() == BodyType.addBelEnd)
                    result = ag.brf(body,null,curInt, true);
                else
                    result = ag.brf(body,null,curInt); // use default (well documented and used) method in case someone has overridden it
                if (result != null) { // really added something
                    // generate events
                    if (!updateEvents(result,newfocus) || !isSameFocus) {
                        removeActionReQueue(curInt);
                    }
                } else {
                    removeActionReQueue(curInt);
                }
            } catch (RevisionFailedException re) {
                generateGoalDeletion(curInt, null, null);
            }
            break;

        case delBelNewFocus:
        case delBel:

            newfocus = Intention.EmptyInt;
            isSameFocus = setts.sameFocus() && h.getBodyType() != BodyType.delBelNewFocus;
            if (isSameFocus) {
                newfocus = curInt;
                body = prepareBodyForEvent(body, u, newfocus.peek());
            } else {
                body = prepareBodyForEvent(body, u, null);
            }
            // call BRF
            try {
                List<Literal>[] result = ag.brf(null, body, curInt); // the intention is not the new focus
                if (result != null) { // really change something
                    // generate events
                    if (!updateEvents(result,newfocus) || !isSameFocus) {
                        removeActionReQueue(curInt);
                    }
                } else {
                    removeActionReQueue(curInt);
                }
            } catch (RevisionFailedException re) {
                generateGoalDeletion(curInt, null, null);
            }
            break;
        }
    }

    // add the self source in the body in case no other source was given
    private Literal prepareBodyForEvent(Literal body, Unifier u, IntendedMeans imRenamedVars) {
        body = (Literal)body.capply(u);
        Unifier renamedVars = new Unifier();
        //getLogger().info("antes "+body+" "+u+" ");
        body.makeVarsAnnon(renamedVars); // free variables in an event cannot conflict with those in the plan
        //getLogger().info("depois "+body+" "+renamedVars);
        if (imRenamedVars != null) {
            imRenamedVars.renamedVars = renamedVars;

            // Code for TRO (Tail Recursion Opt)
            if (setts.isTROon()) {
                // renamed vars binded with another var in u need to be preserved (since u will be lost in TRO)
                Map<VarTerm, Term> adds = null;
                for (VarTerm v: renamedVars) {
                    Term t = u.function.get(v);
                    if (t != null && t.isVar()) {
                        //getLogger().info("adding "+t+"="+v+"="+renamedVars.function.get(v)+" u="+u);
                        if (adds == null)
                            adds = new HashMap<>();
                        try {
                            adds.put((VarTerm)t,renamedVars.function.get(v));
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "*** Error adding var into renamed vars. var="+v+", value="+t+".", e);
                        }
                    }
                }
                if (adds != null)
                    renamedVars.function.putAll(adds);
                // end code for TRO
            }
        }

        body = body.forceFullLiteralImpl();
        if (!body.hasSource()) { // do not add source(self) in case the programmer set the source
            body.addAnnot(BeliefBase.TSelf);
        }
        return body;
    }

    public void applyClrInt(Intention i) throws JasonException {
        while (true) { // quit the method by return
            // Rule ClrInt
            if (i == null)
                return;

            if (i.isFinished()) {
                // intention finished, remove it
                C.dropRunningIntention(i);
                //C.SI = null;
                return;
            }

            IntendedMeans im = i.peek();
            if (!im.isFinished()) {
                // nothing to do
                return;
            }

            // new JasonER
            if (im.getPlan().hasGoalCondition()) { // old test (bug): i.hasGoalCondition() &&
                // move to PI
                C.dropIntention(i);
                C.addPendingIntention(""+i.getId(), ASSyntax.createAtom("wait_goal_condition"), i, false);
                return; // they are cleared by applyClrSatInt
            }

            // remove the finished IM from the top of the intention
            IntendedMeans topIM = i.pop();
            Trigger topTrigger = topIM.getTrigger();
            Literal topLiteral = topTrigger.getLiteral();
            if (logger.isLoggable(Level.FINE))
                logger.fine("Returning from IM "+topIM.getPlan().getLabel()+", te="+topTrigger+" unif="+topIM.unif);

            // produce ^!g[state(finished)[reason(achieved)]] event
            if (!topTrigger.isMetaEvent() && topTrigger.isGoal() && hasGoalListener()) {
                for (GoalListener gl: goalListeners) {
                    gl.goalFinished(topTrigger, GoalStates.achieved);
                }
            }

            // if has finished a failure handling IM ...
            if (im.getTrigger().isGoal() && !im.getTrigger().isAddition() && !i.isFinished()) {
                // needs to get rid of the IM until a goal that
                // has failure handling. E.g,
                //   -!b
                //   +!c
                //   +!d
                //   +!b
                //   +!s: !b; !z
                // should became
                //   +!s: !z
                im = i.peek();
                if (im.isFinished()
                        || !(im.unif.unifies(im.getCurrentStep().getBodyTerm(), topLiteral) && im.getCurrentStep().getBodyType() == BodyType.achieve)
                        || im.getCurrentStep().getBodyTerm() instanceof VarTerm) {
                    im = i.pop(); // +!c above
                }
                while (!i.isFinished() &&
                        !(im.unif.unifies(im.getTrigger().getLiteral(), topLiteral) && im.getTrigger().isGoal()) &&
                        !(im.unif.unifies(im.getCurrentStep().getBodyTerm(), topLiteral) && im.getCurrentStep().getBodyType() == BodyType.achieve)) {
                    im = i.pop();
                }
            }

            if (!i.isFinished()) {
                im = i.peek(); // +!s or +?s
                if (!im.isFinished()) {
                    // removes !b or ?s
                    // unifies the final event with the body that called it

                    // new code optimised: handle directly renamed vars for the call
                    // get vars in the unifier that comes from makeVarAnnon (stored in renamedVars)
                    joinRenamedVarsIntoIntentionUnifier(im,topIM.unif);
                    im.removeCurrentStep();
                }
            }
        }
    }

    private void joinRenamedVarsIntoIntentionUnifier(IntendedMeans im, Unifier values) {
        if (im.renamedVars != null) {
            for (VarTerm ov: im.renamedVars.function.keySet()) {
                //System.out.println("looking for a value for "+ov+" in "+im.renamedVars+" and "+topIM.unif);
                UnnamedVar vt = (UnnamedVar)im.renamedVars.function.get(ov);
                //System.out.println("   via "+vt);
                im.unif.unifiesNoUndo(ov, vt); // introduces the renaming in the current unif
                // if vt has got a value from the top (a "return" value), include this value in the current unif
                Term vl = values.function.get(vt);
                //System.out.println(ov+"="+vt+"="+vl);
                if (vl != null) { // vt has value in top
                    //System.out.println("   and found "+vl);
                    vl = vl.capply(values);
                    if (vl.isLiteral())
                        ((Literal)vl).makeVarsAnnon();
                    im.unif.bind(vt, vl);
                }
            }
        }

    }

    /**********************************************/
    /* auxiliary functions for the semantic rules */
    /**********************************************/

    public List<Option> relevantPlans(Trigger teP, Event evt) throws JasonException {
        Trigger te = teP.clone();
        List<Option> rp = null;

        // gets the proper plan library (root, inner scope, ...)
        PlanLibrary plib = ag.getPL();
        if (evt != null && evt.isInternal() && !evt.getIntention().isFinished()) {
            Plan p = evt.getIntention().peek().getPlan();
            if (p.hasSubPlans()) {
                plib = p.getSubPlans();
            } else {
                plib = p.getScope();
            }
        }


        while (plib != null) {
            List<Plan> candidateRPs = plib.getCandidatePlans(te);
            if (candidateRPs != null) {
                for (Plan pl : candidateRPs) {

                    Unifier relUn = null;
                    if (evt != null && evt.isInternal()) {
                        // use IM vars in the context for sub-plans (new in JasonER)
                        for (IntendedMeans im: evt.getIntention()) {
                            if (im.getPlan().hasSubPlans() && im.getPlan().getSubPlans().get(pl.getLabel()) != null) {
                                relUn = im.triggerUnif.clone();
                                break;
                            }
                        }
                    }

                    relUn = pl.isRelevant(te, relUn);
                    if (relUn != null) {
                        if (rp == null) rp = new LinkedList<>();
                        rp.add(new Option(pl, relUn));
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
        synchronized (C.syncApPlanSense) {
            List<Option> ap = null;
            if (rp != null) {
                //ap = new ApplPlanTimeOut().get(rp);

                for (Option opt: rp) {
                    LogicalFormula context = opt.getPlan().getContext();
                    if (getLogger().isLoggable(Level.FINE))
                        getLogger().log(Level.FINE, "option for "+C.SE.getTrigger()+" is plan "+opt.getPlan().getLabel() + " " + opt.getPlan().getTrigger() + " : " + context + " -- with unification "+opt.getUnifier());

                    if (context == null) { // context is true
                        if (ap == null) ap = new LinkedList<>();
                        ap.add(opt);
                        if (getLogger().isLoggable(Level.FINE))
                            getLogger().log(Level.FINE, "     "+opt.getPlan().getLabel() + " is applicable with unification "+opt.getUnifier());
                    } else {
                        boolean allUnifs = opt.getPlan().isAllUnifs();


                        Iterator<Unifier> r = context.logicalConsequence(ag, opt.getUnifier());
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
                                    opt = new Option(opt.getPlan(), null);
                                }
                            }
                        }

                        if (!isApplicable && getLogger().isLoggable(Level.FINE))
                            getLogger().log(Level.FINE, "     "+opt.getPlan().getLabel() + " is not applicable");
                    }
                }
            }
            return ap;
        }
    }

    public boolean updateEvents(List<Literal>[] result, Intention focus) {
        if (result == null) return false;
        boolean evtAdded = false;
        // create the events
        for (Literal ladd: result[0]) {
            if (!ladd.isRule()) {
                Trigger te = new Trigger(TEOperator.add, TEType.belief, ladd);
                updateEvents(new Event(te, focus));
                evtAdded = true;
                focus = Intention.EmptyInt;
            }
        }
        for (Literal lrem: result[1]) {
            if (!lrem.isRule()) {
                Trigger te = new Trigger(TEOperator.del, TEType.belief, lrem);
                updateEvents(new Event(te, focus));
                evtAdded = true;
                focus = Intention.EmptyInt;
            }
        }
        return evtAdded;
    }

    // only add External Event if it is relevant in respect to the PlanLibrary
    public void updateEvents(Event e) {
        // Note: we have to add events even if they are not relevant to
        // a) allow the user to override selectOption and then provide an "unknown" plan; or then
        // b) create the failure event (it is done by SelRelPlan)
        //if (e.isInternal() || C.hasListener() || ag.getPL().hasCandidatePlan(e.trigger)) {
        // complex to optimise (the above if) in JasonER. removed until we have a good implementation
            C.addEvent(e);
            if (logger.isLoggable(Level.FINE)) logger.fine("Added event " + e+ ", events = "+C.getEvents());
        //}
    }

    /** remove the top action and requeue the current intention */
    private void removeActionReQueue(Intention i) {
        if (!i.isFinished()) {
            i.peek().removeCurrentStep();
            C.addRunningIntention(i);
        } else {
            logger.fine("trying to update a finished intention!");
        }
    }

    /** generate a failure event for an intention */
    public boolean generateGoalDeletion(Intention i, List<Term> failAnnots, Term reason) throws JasonException {
        boolean failEventIsRelevant = false;
        IntendedMeans im = i.peek();
        // produce failure event
        Event failEvent = findEventForFailure(i, im.getTrigger());
        if (failEvent != null) {
            failEventIsRelevant = true;
        } else {
            failEvent = new Event(im.getTrigger().clone(), i);
        }
        if (im.getCurrentStep() != null) {
            Term bodyPart = im.getCurrentStep().getBodyTerm().capply(im.unif);
            setDefaultFailureAnnots(failEvent, bodyPart, failAnnots);
        }

        if (im.getTrigger().isGoal()) {

            // notify listeners
            if (hasGoalListener())
                for (GoalListener gl: goalListeners) {
                    gl.goalFailed(im.getTrigger(), reason);
                    if (failEventIsRelevant) {
                        gl.goalExecuting(im.getTrigger(), ASSyntax.createAtom("has_failure_plan"));
                    } else {
                        gl.goalFinished(im.getTrigger(), null);
                        // finish other goals in the stack
                        for (IntendedMeans oim: i) {
                            if (oim != im)
                                gl.goalFinished(oim.getTrigger(), GoalStates.failed);
                        }
                    }
                }

            if (failEventIsRelevant) {
                C.addEvent(failEvent);
                if (logger.isLoggable(Level.FINE)) logger.fine("Generating goal deletion " + failEvent.getTrigger() + " from goal: " + im.getTrigger());
            } else {
                logger.warning("No failure event was generated for " + failEvent.getTrigger() + "\n"+i);
                i.fail(getC());
            }
        }
        // if "discard" is set, we are deleting the whole intention!
        // it is simply not going back to 'I' nor anywhere else!
        else if (setts.requeue()) {
            // get the external event (or the one that started
            // the whole focus of attention) and requeue it
            im = i.peek(); //get(0);
            C.addExternalEv(im.getTrigger());
        } else {
            logger.warning("Could not finish intention: " + i + "\tTrigger: " + failEvent.getTrigger());
        }
        return failEventIsRelevant;
    }

    // similar to the one above, but for an Event rather than intention
    private boolean generateGoalDeletionFromEvent(List<Term> failAnnots, Term reason) throws JasonException {
        Event ev = C.SE;
        if (ev == null) {
            logger.warning("** It was impossible to generate a goal deletion event because SE is null! " + C);
            return false;
        }

        Trigger tevent = ev.trigger;
        boolean failEeventGenerated = false;
        if (tevent.isAddition() && tevent.isGoal()) {
            // produce failure event
            Event failEvent = findEventForFailure(ev.intention, tevent);

            if (failEvent != null) {
                setDefaultFailureAnnots(failEvent, tevent.getLiteral(), failAnnots);
                C.addEvent(failEvent);
                failEeventGenerated = true;
                //logger.warning("Generating goal deletion " + failEvent.getTrigger() + " from event: " + ev.getTrigger());
            } else {
                logger.warning("No fail event was generated for " + ev.getTrigger());
                if (ev.intention != null) {
                    ev.intention.fail(getC());
                    logger.warning("\n"+ev.intention);
                }
            }

            // notify listener
            if (hasGoalListener())
                for (GoalListener gl: goalListeners) {
                    gl.goalFailed(tevent, reason);
                    if (failEeventGenerated) {
                        gl.goalExecuting(tevent, ASSyntax.createAtom("has_failure_plan"));
                    } else {
                        gl.goalFinished(tevent, null);
                        if (ev.getIntention() != null)
                            for (IntendedMeans im: ev.getIntention()) {
                                gl.goalFinished(im.getTrigger(), GoalStates.failed);
                            }
                    }
                }

        } else if (ev.isInternal()) {
            logger.warning("Could not finish intention:\n" + ev.intention);
        }
        // if "discard" is set, we are deleting the whole intention!
        // it is simply not going back to I nor anywhere else!
        else if (setts.requeue()) {
            C.addEvent(ev);
            logger.warning("Requeing external event: " + ev);
        } else
            logger.warning("Discarding external event: " + ev);
        return failEeventGenerated;
    }

    public Event findEventForFailure(Intention i, Trigger tevent) {
        if (i != Intention.EmptyInt) {
            return i.findEventForFailure(tevent, getAg().getPL(), getC()).getFirst();
        } else if (tevent.isGoal() && tevent.isAddition()) {
            Trigger failTrigger = new Trigger(TEOperator.del, tevent.getType(), tevent.getLiteral());
            if (getAg().getPL().hasCandidatePlan(failTrigger))
                return new Event(failTrigger.clone(), i);
        }
        return null;
    }

    /** add default error annotations (error, error_msg, code, code_src, code_line) in the failure event */
    private static void setDefaultFailureAnnots(Event failEvent, Term body, List<Term> failAnnots) {
        // add default failure annots
        if (failAnnots == null)
            failAnnots = JasonException.createBasicErrorAnnots( JasonException.UNKNOW_ERROR, "");

        Literal eventLiteral = failEvent.getTrigger().getLiteral().forceFullLiteralImpl();
        eventLiteral.addAnnots(failAnnots);

        // add failure annots in the event related to the code source
        Literal bodyterm = SourceInfo.aNOCODE;
        if (body != null && body instanceof Literal) {
            bodyterm = (Literal)body;
            eventLiteral.addSourceInfoAsAnnots( ((Literal)body).getSrcInfo() );
        }

        // code
        if (eventLiteral.getAnnot("code") == null)
            eventLiteral.addAnnot(ASSyntax.createStructure("code", bodyterm.copy().makeVarsAnnon()));
    }

    /*
     * check if the event is a goal addition with hard deadline, if so, schedule a verification after the deadline
     */
    protected void checkHardDeadline(final Event evt) {
        final Literal body = evt.getTrigger().getLiteral();
        Literal hdl  = body.getAnnot(ASSyntax.hardDeadLineStr);
        if (hdl == null)
            return;
        if (hdl.getArity() < 1)
            return;

        // schedule the verification of deadline for the intention
        final Intention intention = evt.getIntention();
        final int isize;
        if (intention == null)
            isize = 0;
        else
            isize = intention.size();
        int deadline = 0;
        try {
            deadline = (int)((NumberTerm)hdl.getTerm(0)).solve();
        } catch (NoValueException e1) {
            e1.printStackTrace();
        }

        Agent.getScheduler().schedule(() -> {
                runAtBeginOfNextCycle(() -> {
                        boolean drop = false;
                        if (intention == null) { // deadline in !!g, test if the agent still desires it
                            drop = desire.allDesires(C, body, null, new Unifier()).hasNext();
                        } else if (intention.size() >= isize && intention.hasTrigger(evt.getTrigger(), new Unifier())) {
                            drop = true;
                        }
                        if (drop) {
                            try {
                                new fail_goal() {
                                    protected void addAnnotsToFailEvent(Event failEvent) {
                                        failEvent.getTrigger().getLiteral().addAnnots(JasonException.createBasicErrorAnnots("deadline_reached", ""));
                                    }
                                }.drop(TransitionSystem.this, body, new Unifier());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                });
                getAgArch().wakeUpSense();
        }, deadline, TimeUnit.MILLISECONDS);
    }

    public boolean canSleep() {
        return    (C.isAtomicIntentionSuspended() && !C.hasFeedbackAction() && !C.hasMsg())  // atomic case
                  || (!C.hasEvent() &&    // other cases (deliberate)
                      !C.hasRunningIntention() && !C.hasFeedbackAction() && // (action)
                      !C.hasMsg() &&  // (sense)
                      taskForBeginOfCycle.isEmpty() &&
                      getAgArch().canSleep());
    }

    public boolean canSleepSense() {
        return !C.hasMsg() && getAgArch().canSleep();
    }

    public boolean canSleepDeliberate() {
        return !C.hasEvent() && taskForBeginOfCycle.isEmpty() && C.getSelectedEvent() == null && getAgArch().canSleep();
    }

    public boolean canSleepAct() {
        //&& !C.hasPendingAction()
        return !C.hasRunningIntention() && !C.hasFeedbackAction() && C.getSelectedIntention() == null && getAgArch().canSleep();
    }

    /**
     * Schedule a task to be executed in the beginning of the next reasoning cycle.
     * It is used mostly to change the C only by the TS thread (e.g. by .wait)
     */
    public void runAtBeginOfNextCycle(RunnableSerializable r) {
        taskForBeginOfCycle.offer(r);
        getAgArch().wake();
    }

    /**********************************************************************/
    /* MAIN LOOP */
    /**********************************************************************/
    /* infinite loop on one reasoning cycle                               */
    /* plus the other parts of the agent architecture besides             */
    /* the actual transition system of the AS interpreter                 */
    /**********************************************************************/

    public void reasoningCycle() {
        sense();
        deliberate();
        act();
    }

    public void sense() {
        try {
            if (logger.isLoggable(Level.FINE)) logger.fine("Start sense");

            C.resetSense();

            if (nrcslbr >= setts.nrcbp()) {
                nrcslbr = 0;
                synchronized (C.syncApPlanSense) {
                    ag.buf(getAgArch().perceive());
                }
                getAgArch().checkMail();
            }
            nrcslbr++; // counting number of cycles since last belief revision

            // produce sleep events
            if (ag.pl.hasJagPlans()) {
                if (canSleep()) {
                    if (!sleepingEvt) {
                        sleepingEvt = true;
                        C.addExternalEv(PlanLibrary.TE_JAG_SLEEPING);
                    }
                } else if (sleepingEvt) { // code to turn idleEvt false again
                    sleepingEvt = false;
                    C.addExternalEv(PlanLibrary.TE_JAG_AWAKING);
                }
            }

            stepSense = State.StartRC;
            do {
                applySemanticRuleSense();
            } while (stepSense != State.SelEv && getAgArch().isRunning());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "*** ERROR in the transition system (sense). "+C+"\nCreating a new C!", e);
            C.create();
        }
    }

    public void deliberate() {
        try {
            C.resetDeliberate();

            // run tasks allocated to be performed in the beginning of the cycle
            Runnable r = taskForBeginOfCycle.poll();
            while (r != null) {
                r.run(); //It is processed only things related to operations on goals/intentions resumed/suspended/finished It can be placed in the deliberate stage, but the problem is the sleep when the synchronous execution is adopted
                r = taskForBeginOfCycle.poll();
            }

            stepDeliberate = State.SelEv;
            do {
                applySemanticRuleDeliberate();
            } while (stepDeliberate != State.ProcAct && getAgArch().isRunning());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "*** ERROR in the transition system (deliberate). "+C+"\nCreating a new C!", e);
            C.create();
        }
    }

    public void act() {
        try {
            C.resetAct();

            stepAct = State.ProcAct;
            do {
                applySemanticRuleAct();
            } while (stepAct != State.StartRC && getAgArch().isRunning());


            ActionExec action = C.getAction();
            if (action != null) {
                C.addPendingAction(action);
                // We need to send a wrapper for FA to the user so that add method then calls C.addFA (which control atomic things)
                getAgArch().act(action); //, C.getFeedbackActionsWrapper());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "*** ERROR in the transition system (act). "+C+"\nCreating a new C!", e);
            C.create();
        }
    }

    /*
    public boolean reasoningCycle() {
        if (logger.isLoggable(Level.FINE)) logger.fine("Start new reasoning cycle");
        getUserAgArch().reasoningCycleStarting();
      */
    /* used to find bugs (ignore)
    int is = C.getIntentions().size();
    int es = C.getEvents().size();
    if (is+es != 4) {
        logger.info("1****"+is+" "+es);
        logger.info(C.toString());
        try {
            logger.info("======"+applyClrInt(C.SI));
            logger.info("======"+C.SI.isFinished());
            logger.info("======"+C.SI.getB());
            //logger.info(""+getAg().getPL());
        } catch (JasonException e) {
            e.printStackTrace();
        }
        logger.info("old 1"+pc1.toString());
        logger.info("old 2"+pc2.toString());
        logger.info("old 3"+pc3.toString());
        logger.info("old 4"+pc4.toString());
        System.exit(0);
        return false;
    }

    pc4 = pc3;
    pc3 = pc2;
    pc2 = pc1;
    pc1 = C.clone();*/
    /*
         try {
             C.reset();

             // run tasks allocated to be performed in the beginning of the cycle
             Runnable r = taskForBeginOfCycle.poll();
             while (r != null) {
                 r.run();
                 r = taskForBeginOfCycle.poll();
             }

             if (nrcslbr >= setts.nrcbp()) {
                 nrcslbr = 0;
                 ag.buf(getUserAgArch().perceive());
                 getUserAgArch().checkMail();
             }
             nrcslbr++; // counting number of cycles since last belief revision

             if (canSleep()) {
                 if (!sleepingEvt) {
                     sleepingEvt = true;
                     if (ag.pl.getCandidatePlans(PlanLibrary.TE_JAG_SLEEPING) != null)
                         C.addExternalEv(PlanLibrary.TE_JAG_SLEEPING);
                 } else {
                     getUserAgArch().sleep();
                     return false;
                 }
             } else if (sleepingEvt) { // code to turn idleEvt false again
                 if (C.hasMsg()) { // the agent has messages
                     sleepingEvt = false;
                 } else if (C.hasEvent()) {
                     // check if there is an event in C.E not produced by idle intention
                     for (Event e: C.getEvents()) {
                         Intention i = e.getIntention();
                         if ( !e.getTrigger().equals(PlanLibrary.TE_JAG_SLEEPING)
                              ||
                              (i != null && i.hasTrigger(PlanLibrary.TE_JAG_SLEEPING, new Unifier()))
                            ) {
                             sleepingEvt = false;
                             break;
                         }
                     }
                 }
                 if (!sleepingEvt && ag.pl.getCandidatePlans(PlanLibrary.TE_JAG_AWAKING) != null) {
                     C.addExternalEv(PlanLibrary.TE_JAG_AWAKING);
                 }
             }

             step = State.StartRC;
             do {
                 if (!getUserAgArch().isRunning()) return false;
                 applySemanticRule();
             } while (step != State.StartRC);

             ActionExec action = C.getAction();
             if (action != null) {
                 C.addPendingAction(action);
                 // We need to send a wrapper for FA to the user so that add method then calls C.addFA (which control atomic things)
                 getUserAgArch().act(action, C.getFeedbackActionsWrapper());
             }

         } catch (Exception e) {
             logger.log(Level.SEVERE, "*** ERROR in the transition system. "+C+"\nCreating a new C!", e);
             C.create();
         }

         return true;
     }
    */
    // Auxiliary functions
    // (for Internal Actions to be able to access the configuration)
    public Agent getAg() {
        return ag;
    }

    public Circumstance getC() {
        return C;
    }

    /*public State getStep() {
        return step;
    }*/

    public Settings getSettings() {
        return setts;
    }

    public void setAgArch(AgArch arch) {
        agArch = arch;
    }
    public AgArch getAgArch() {
        if (agArch == null)
            return null;
        else
            return agArch.getFirstAgArch();
    }

    /**
     *
     * @deprecated use getAgArch
     */
    @Deprecated
    public AgArch getUserAgArch() {
        return agArch;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return "TS of agent "+getAgArch().getAgName();
    }
}
