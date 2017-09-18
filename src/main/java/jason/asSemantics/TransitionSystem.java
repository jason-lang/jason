package jason.asSemantics;

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
import jason.asSemantics.GoalListener.FinishStates;
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
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.PlanLibrary;
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
import jason.util.Config;


public class TransitionSystem {

    public enum State { StartRC, SelEv, RelPl, ApplPl, SelAppl, FindOp, AddIM, ProcAct, SelInt, ExecInt, ClrInt }

    private Logger        logger     = null;

    private Agent         ag         = null;
    private AgArch        agArch     = null;
    private Circumstance  C          = null;
    private Settings      setts      = null;
    //private State         step       = State.StartRC; // first step of the SOS

    private State         stepSense       = State.StartRC;
    private State         stepDeliberate  = State.SelEv;
    private State         stepAct         = State.ProcAct;


    private int           nrcslbr    = Settings.ODefaultNRC; // number of reasoning cycles since last belief revision

    private boolean       sleepingEvt    = false;

    private List<GoalListener>  goalListeners = null;

    // both configuration and configuration' point to this
    // object, this is just to make it look more like the SOS
    private TransitionSystem      confP;
    private TransitionSystem      conf;

    private Queue<Runnable> taskForBeginOfCycle = new ConcurrentLinkedQueue<Runnable>();

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

        // we need to initialise this "aliases"
        conf = confP = this;

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
            goalListeners = new ArrayList<GoalListener>();
            listenersMap  = new HashMap<GoalListener, CircumstanceListener>();
        } else {
            // do not install two MetaEventGoalListener
            for (GoalListener g: goalListeners)
                if (g instanceof GoalListenerForMetaEvents)
                    return;
        }

        // we need to add a listener in C to map intention events to goal events
        CircumstanceListener cl = new CircumstanceListener() {

            public void intentionDropped(Intention i) {
                for (IntendedMeans im: i) //.getIMs())
                    if (im.getTrigger().isAddition() && im.getTrigger().isGoal())
                        gl.goalFinished(im.getTrigger(), FinishStates.dropped);
            }

            public void intentionSuspended(Intention i, String reason) {
                for (IntendedMeans im: i) //.getIMs())
                    if (im.getTrigger().isAddition() && im.getTrigger().isGoal())
                        gl.goalSuspended(im.getTrigger(), reason);
            }

            public void intentionResumed(Intention i) {
                for (IntendedMeans im: i) //.getIMs())
                    if (im.getTrigger().isAddition() && im.getTrigger().isGoal())
                        gl.goalResumed(im.getTrigger());
            }

            public void eventAdded(Event e) {
                if (e.getTrigger().isAddition() && e.getTrigger().isGoal())
                    gl.goalStarted(e);
            }

            public void intentionAdded(Intention i) {  }
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
        CircumstanceListener cl = listenersMap.get(gl);
        if (cl != null) C.removeEventListener(cl);
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
            confP.stepAct = State.StartRC;
            applyClrInt(conf.C.SI);
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
        case ClrInt:    confP.step = State.StartRC;
                        applyClrInt(conf.C.SI);
                        break;
        }
    }
    */

    // the semantic rules are referred to in comments in the functions below

    private final String kqmlReceivedFunctor = Config.get().getKqmlFunctor();

    private void applyProcMsg() throws JasonException {
        confP.stepSense = State.SelEv;
        if (conf.C.hasMsg()) {
            Message m = conf.ag.selectMessage(conf.C.getMailBox());
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
                // the send that put the intention in Pending state was
                // something like
                //    .send(ag1,askOne, value, X)
                // if the answer was tell 3, unifies X=3
                // if the answer was untell 3, unifies X=false
                Structure send = (Structure)intention.peek().getCurrentStep().getBodyTerm();
                if (m.isUnTell() && send.getTerm(1).toString().equals("askOne")) {
                    content = Literal.LFalse;
                } else if (content.isLiteral()) { // adds source in the content if possible
                    content = ((Literal)content).forceFullLiteralImpl();
                    ((Literal)content).addSource(new Atom(m.getSender()));
                } else if (send.getTerm(1).toString().equals("askAll") && content.isList()) { // adds source in each answer if possible
                    ListTerm tail = new ListTermImpl();
                    for (Term t: ((ListTerm)content)) {
                        if (t.isLiteral()) {
                            t = ((Literal)t).forceFullLiteralImpl();
                            ((Literal)t).addSource(new Atom(m.getSender()));
                        }
                        tail.append(t);
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
                    int nbReceivers = ((ListTerm)send.getTerm(0)).size();
                    if (listOfAnswers.size() == nbReceivers) { // all agents have answered
                        resumeSyncAskIntention(m.getInReplyTo(), send.getTerm(3), listOfAnswers);
                    }
                } else {
                    resumeSyncAskIntention(m.getInReplyTo(), send.getTerm(3), content);
                }

                // the message is not an ask answer
            } else if (conf.ag.socAcc(m)) {

                if (! m.isReplyToSyncAsk()) { // ignore answer after the timeout
                    // generate an event
                    String sender = m.getSender();
                    if (sender.equals(getUserAgArch().getAgName()))
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
                        }
                    }

                    if (!added) {
                        Literal received = new LiteralImpl(kqmlReceivedFunctor).addTerms(
                            new Atom(sender),
                            new Atom(m.getIlForce()),
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
            getC().resumeIntention(i);
        } else {
            generateGoalDeletion(i, JasonException.createBasicErrorAnnots("ask_failed", "reply of an ask message ('"+answerValue+"') does not unify with fourth argument of .send ('"+answerVar+"')"));
        }

    }

    private void applySelEv() throws JasonException {

        // Rule for atomic, if there is an atomic intention, do not select event
        if (C.hasAtomicIntention()) {
            confP.stepDeliberate = State.ProcAct; // need to go to ProcAct to see if an atomic intention received a feedback action
            return;
        }

        // Rule for atomic, events from atomic intention have priority
        confP.C.SE = C.removeAtomicEvent();
        if (confP.C.SE != null) {
            confP.stepDeliberate = State.RelPl;
            return;
        }

        if (conf.C.hasEvent()) {
            // Rule SelEv1
            confP.C.SE = conf.ag.selectEvent(confP.C.getEvents());
            if (logger.isLoggable(Level.FINE))
                logger.fine("Selected event "+confP.C.SE);
            if (confP.C.SE != null) {
                if (ag.hasCustomSelectOption() || setts.verbose() == 2) // verbose == 2 means debug mode
                    confP.stepDeliberate = State.RelPl;
                else
                    confP.stepDeliberate = State.FindOp;
                return;
            }
        }
        // Rule SelEv2
        // directly to ProcAct if no event to handle
        confP.stepDeliberate = State.ProcAct;
    }

    private void applyRelPl() throws JasonException {
        // get all relevant plans for the selected event
        confP.C.RP = relevantPlans(conf.C.SE.trigger);

        // Rule Rel1
        if (confP.C.RP != null || setts.retrieve())
            // retrieve is mainly for Coo-AgentSpeak
            confP.stepDeliberate = State.ApplPl;
        else
            applyRelApplPlRule2("relevant");
    }

    private void applyApplPl() throws JasonException {
        confP.C.AP = applicablePlans(confP.C.RP);

        // Rule Appl1
        if (confP.C.AP != null || setts.retrieve())
            // retrieve is mainly for Coo-AgentSpeak
            confP.stepDeliberate = State.SelAppl;
        else
            applyRelApplPlRule2("applicable");
    }

    /** generates goal deletion event */
    private void applyRelApplPlRule2(String m) throws JasonException {
        confP.stepDeliberate = State.ProcAct; // default next step
        if (conf.C.SE.trigger.isGoal() && !conf.C.SE.trigger.isMetaEvent()) {
            // can't carry on, no relevant/applicable plan.
            try {
                if (conf.C.SE.getIntention() != null && conf.C.SE.getIntention().size() > 3000) {
                    logger.warning("we are likely in a problem with event "+conf.C.SE.getTrigger()+" the intention stack has already "+conf.C.SE.getIntention().size()+" intended means!");
                }
                String msg = "Found a goal for which there is no "+m+" plan:" + conf.C.SE.getTrigger();
                if (!generateGoalDeletionFromEvent(JasonException.createBasicErrorAnnots("no_"+m, msg))) {
                    logger.warning(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        } else if (conf.C.SE.isInternal()) {
            // e.g. belief addition as internal event, just go ahead
            // but note that the event was relevant, yet it is possible
            // the programmer just wanted to add the belief and it was
            // relevant by chance, so just carry on instead of dropping the
            // intention
            Intention i = conf.C.SE.intention;
            joinRenamedVarsIntoIntentionUnifier(i.peek(), i.peek().unif);
            updateIntention(i);
        } else if (setts.requeue()) {
            // if external, then needs to check settings
            confP.C.addEvent(conf.C.SE);
        } else {
            // current event is external and irrelevant,
            // discard that event and select another one
            confP.stepDeliberate = State.SelEv;
        }
    }


    private void applySelAppl() throws JasonException {
        // Rule SelAppl
        confP.C.SO = conf.ag.selectOption(confP.C.AP);

        if (confP.C.SO != null) {
            confP.stepDeliberate = State.AddIM;
            if (logger.isLoggable(Level.FINE)) logger.fine("Selected option "+confP.C.SO+" for event "+confP.C.SE);
        } else {
            logger.fine("** selectOption returned null!");
            generateGoalDeletionFromEvent(JasonException.createBasicErrorAnnots("no_option", "selectOption returned null"));
            // can't carry on, no applicable plan.
            confP.stepDeliberate = State.ProcAct;
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
        confP.stepDeliberate = State.AddIM; // default next step

        // get all relevant plans for the selected event
        //Trigger te = (Trigger) conf.C.SE.trigger.clone();
        List<Plan> candidateRPs = conf.ag.pl.getCandidatePlans(conf.C.SE.trigger);
        if (candidateRPs != null) {
            for (Plan pl : candidateRPs) {
                Unifier relUn = pl.isRelevant(conf.C.SE.trigger);
                if (relUn != null) { // is relevant
                    LogicalFormula context = pl.getContext();
                    if (context == null) { // context is true
                        confP.C.SO = new Option(pl, relUn);
                        return;
                    } else {
                        Iterator<Unifier> r = context.logicalConsequence(ag, relUn);
                        if (r != null && r.hasNext()) {
                            confP.C.SO = new Option(pl, r.next());
                            return;
                        }
                    }
                }
            }
            applyRelApplPlRule2("applicable");
        } else {
            // problem: no plan
            applyRelApplPlRule2("relevant");
        }
    }

    private void applyAddIM() throws JasonException {
        // create a new intended means
        IntendedMeans im = new IntendedMeans(conf.C.SO, conf.C.SE.getTrigger());

        // Rule ExtEv
        if (conf.C.SE.intention == Intention.EmptyInt) {
            Intention intention = new Intention();
            intention.push(im);
            confP.C.addIntention(intention);
        } else {
            // Rule IntEv

            // begin tail recursion optimisation (TRO)
            if (setts.isTROon()) {
                IntendedMeans top = confP.C.SE.intention.peek(); // top = the IM that will be removed from the intention due to TRO
                //System.out.println(top.getTrigger().isGoal()+"=1="+im.getTrigger().isGoal());
                //System.out.println(top.getTrigger().getLiteral().getPredicateIndicator()+"=2="+im.getTrigger().getLiteral().getPredicateIndicator());
                if (top != null && top.getTrigger().isGoal() && im.getTrigger().isGoal() && // are both goal
                        top.getCurrentStep().getBodyNext() == null && // the plan below is finished
                        top.getTrigger().getLiteral().getPredicateIndicator().equals( im.getTrigger().getLiteral().getPredicateIndicator()) // goals are equals (do not consider - or + from the trigger -- required in the case of goal patterns where -!g <- !g is used)
                   ) {
                    confP.C.SE.intention.pop(); // remove the top IM

                    IntendedMeans imBase = confP.C.SE.intention.peek(); // base = where the new IM will be place on top of
                    if (imBase != null && imBase.renamedVars != null) {
                        // move top relevant values into the base (relevant = renamed vars in base)
                        
                        for (VarTerm v: imBase.renamedVars) {
                            VarTerm vvl = (VarTerm)imBase.renamedVars.function.get(v);
                            Term t = top.unif.get(vvl);
                            if (t != null) { // if v has got a value in top unif, put the value in the unifier
                                if (t instanceof Literal) {
                                    Literal l= (Literal)t.capply(top.unif);
                                    l.makeVarsAnnon(top.renamedVars);
                                    im.unif.function.put(vvl, l);
                                } else {
                                    im.unif.function.put(vvl, t);
                                }
                            } else {
                                // the vvl was renamed again in top, just replace in base the new value
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

            confP.C.SE.intention.push(im);
            confP.C.addIntention(confP.C.SE.intention);
        }
        confP.stepDeliberate = State.ProcAct;
    }

    private void applyProcAct() throws JasonException {
        confP.stepAct = State.SelInt; // default next step
        if (conf.C.hasFeedbackAction()) {
            ActionExec a = null;
            synchronized (conf.C.getFeedbackActions()) {
                a = conf.ag.selectAction(conf.C.getFeedbackActions());
            }
            if (a != null) {
                final Intention curInt = a.getIntention();

                // remove the intention from PA (PA has all pending action, including those in FA;
                // but, if the intention is not in PA, it means that the intention was dropped
                // and should not return to I)
                if (C.removePendingAction(curInt.getId()) != null) {
                    if (a.getResult()) {
                        // add the intention back in I
                        updateIntention(curInt);
                        applyClrInt(curInt);

                        if (hasGoalListener())
                            for (GoalListener gl: getGoalListeners())
                                for (IntendedMeans im: curInt) //.getIMs())
                                    gl.goalResumed(im.getTrigger());
                    } else {
                        String reason = a.getFailureMsg();
                        if (reason == null) reason = "";
                        ListTerm annots = JasonException.createBasicErrorAnnots("action_failed", reason);
                        if (a.getFailureReason() != null)
                            annots.append(a.getFailureReason());
                        generateGoalDeletion(curInt, annots);
                        C.removeAtomicIntention(); // if (potential) atomic intention is not removed, it will be selected in selInt or selEv and runs again
                    }
                } else {
                    applyProcAct(); // get next action
                }
            }
        }
    }

    private void applySelInt() throws JasonException {
        confP.stepAct = State.ExecInt; // default next step

        // Rule for Atomic Intentions
        confP.C.SI = C.removeAtomicIntention();
        if (confP.C.SI != null) {
            return;
        }

        // Rule SelInt1
        if (!conf.C.isAtomicIntentionSuspended() && conf.C.hasIntention()) { // the isAtomicIntentionSuspended is necessary because the atomic intention may be suspended (the above removeAtomicInt returns null in that case)
            // but no other intention could be selected
            confP.C.SI = conf.ag.selectIntention(conf.C.getIntentions());
            if (logger.isLoggable(Level.FINE)) logger.fine("Selected intention "+confP.C.SI);
            if (confP.C.SI != null) { // the selectIntention function returned null
                return;
            }
        }

        confP.stepAct = State.StartRC;
    }

    @SuppressWarnings("unchecked")
    private void applyExecInt() throws JasonException {
        confP.stepAct = State.ClrInt; // default next step

        final Intention curInt = conf.C.SI;
        if (curInt == null)
            return;

        if (curInt.isFinished()) {
            return;
        }

        // get next formula in the body of the intended means
        // on the top of the selected intention

        IntendedMeans im = curInt.peek();

        if (im.isFinished()) {
            // for empty plans! may need unif, etc
            updateIntention(curInt);
            return;
        }
        Unifier     u = im.unif;
        PlanBody    h = im.getCurrentStep();

        Term bTerm = h.getBodyTerm();

        if (bTerm instanceof VarTerm) { // de-var bTerm
            bTerm = bTerm.capply(u);
            if (bTerm.isVar()) { // the case of !A with A not ground
                String msg = h.getSrcInfo()+": "+ "Variable '"+bTerm+"' must be ground.";
                if (!generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("body_var_without_value", msg)))
                    logger.log(Level.SEVERE, msg);
                return;
            }
            if (bTerm.isPlanBody()) {
                if (h.getBodyType() != BodyType.action) { // the case of ...; A = { !g }; +g; ....
                    String msg = h.getSrcInfo()+": "+ "The operator '"+h.getBodyType()+"' is lost with the variable '"+bTerm+"' unified with a plan body. ";
                    if (!generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("body_var_with_op", msg)))
                        logger.log(Level.SEVERE, msg);
                    return;
                }
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
            confP.C.A = new ActionExec(body, curInt);
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
                    updateIntention(curInt);
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
                if (!generateGoalDeletion(curInt, errorAnnots))
                    logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage());
                ok = true; // just to not generate the event again

            } catch (JasonException e) {
                errorAnnots = e.getErrorTerms();
                if (!generateGoalDeletion(curInt, errorAnnots))
                    logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage());
                ok = true; // just to not generate the event again
            } catch (Exception e) {
                if (body == null)
                    logger.log(Level.SEVERE, "Selected an intention with null body in '"+h+"' and IM "+im, e);
                else
                    logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage(), e);
            }
            if (!ok)
                generateGoalDeletion(curInt, errorAnnots);

            break;

        case constraint:
            Iterator<Unifier> iu = ((LogicalFormula)bTerm).logicalConsequence(ag, u);
            if (iu.hasNext()) {
                im.unif = iu.next();
                updateIntention(curInt);
            } else {
                String msg = "Constraint "+h+" was not satisfied ("+h.getSrcInfo()+") un="+u;
                generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots(new Atom("constraint_failed"), msg));
                logger.fine(msg);
            }
            break;

        // Rule Achieve
        case achieve:
            body = prepareBodyForEvent(body, u, curInt.peek());
            Event evt = conf.C.addAchvGoal(body, curInt);
            confP.stepAct = State.StartRC;
            checkHardDeadline(evt);
            break;

        // Rule Achieve as a New Focus (the !! operator)
        case achieveNF:
            body = prepareBodyForEvent(body, u, null);
            evt  = conf.C.addAchvGoal(body, Intention.EmptyInt);
            checkHardDeadline(evt);
            updateIntention(curInt);
            break;

        // Rule Test
        case test:
            LogicalFormula f = (LogicalFormula)bTerm;
            if (conf.ag.believes(f, u)) {
                updateIntention(curInt);
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
                            conf.C.addEvent(evt);
                            confP.stepAct = State.StartRC;
                            fail = false;
                        }
                    }
                }
                if (fail) {
                    if (logger.isLoggable(Level.FINE)) logger.fine("Test '"+bTerm+"' failed ("+h.getSrcInfo()+").");
                    generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("test_goal_failed", "Failed to test '"+bTerm+"'"));
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
            // (in future releases, creates a two branches for this operator)

            try {
                List<Literal>[] result = ag.brf(null, b2, curInt); // the intention is not the new focus
                if (result != null) { // really delete something
                    // generate events
                    updateEvents(result,Intention.EmptyInt);
                }
            } catch (RevisionFailedException re) {
                generateGoalDeletion(curInt, JasonException.createBasicErrorAnnots("belief_revision_failed", "BRF failed for '"+body+"'"));
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
                    updateEvents(result,newfocus);
                    if (!isSameFocus) {
                        updateIntention(curInt);
                    }
                } else {
                    updateIntention(curInt);
                }
            } catch (RevisionFailedException re) {
                generateGoalDeletion(curInt, null);
            }
            break;

        case delBel:

            newfocus = Intention.EmptyInt;
            if (setts.sameFocus()) {
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
                    updateEvents(result,newfocus);
                    if (!setts.sameFocus()) {
                        updateIntention(curInt);
                    }
                } else {
                    updateIntention(curInt);
                }
            } catch (RevisionFailedException re) {
                generateGoalDeletion(curInt, null);
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
                            adds = new HashMap<VarTerm, Term>();
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
                confP.C.dropIntention(i);
                //conf.C.SI = null;
                return;
            }

            IntendedMeans im = i.peek();
            if (!im.isFinished()) {
                // nothing to do
                return;
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
                    gl.goalFinished(topTrigger, FinishStates.achieved);
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

                    // old code:
                    /*topLiteral.apply(topIM.unif);
                    topLiteral.makeVarsAnnon();
                    Literal cstep = (Literal)im.removeCurrentStep();
                    boolean r = im.unif.unifies(cstep,topLiteral);
                    */

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

    public List<Option> relevantPlans(Trigger teP) throws JasonException {
        Trigger te = teP.clone();
        List<Option> rp = null;
        List<Plan> candidateRPs = conf.ag.pl.getCandidatePlans(te);
        if (candidateRPs != null) {
            for (Plan pl : candidateRPs) {
                Unifier relUn = pl.isRelevant(te);
                if (relUn != null) {
                    if (rp == null) rp = new LinkedList<Option>();
                    rp.add(new Option(pl, relUn));
                }
            }
        }
        return rp;
    }

    public List<Option> applicablePlans(List<Option> rp) throws JasonException {
        synchronized (C.syncApPlanSense) {
            List<Option> ap = null;
            if (rp != null) {
                //ap = new ApplPlanTimeOut().get(rp);

                for (Option opt: rp) {
                    LogicalFormula context = opt.getPlan().getContext();
                    if (context == null) { // context is true
                        if (ap == null) ap = new LinkedList<Option>();
                        ap.add(opt);
                    } else {
                        boolean allUnifs = opt.getPlan().isAllUnifs();
                        Iterator<Unifier> r = context.logicalConsequence(ag, opt.getUnifier());
                        if (r != null) {
                            while (r.hasNext()) {
                                opt.setUnifier(r.next());

                                if (ap == null) ap = new LinkedList<Option>();
                                ap.add(opt);

                                if (!allUnifs) break; // returns only the first unification
                                if (r.hasNext()) {
                                    // create a new option for the next loop step
                                    opt = new Option(opt.getPlan(), null);
                                }
                            }
                        }
                    }
                }
            }
            return ap;
        }
    }

    public void updateEvents(List<Literal>[] result, Intention focus) {
        if (result == null) return;
        // create the events
        for (Literal ladd: result[0]) {
            Trigger te = new Trigger(TEOperator.add, TEType.belief, ladd);
            updateEvents(new Event(te, focus));
            focus = Intention.EmptyInt;
        }
        for (Literal lrem: result[1]) {
            Trigger te = new Trigger(TEOperator.del, TEType.belief, lrem);
            updateEvents(new Event(te, focus));
            focus = Intention.EmptyInt;
        }
    }

    // only add External Event if it is relevant in respect to the PlanLibrary
    public void updateEvents(Event e) {
        // Note: we have to add events even if they are not relevant to
        // a) allow the user to override selectOption and then provide an "unknown" plan; or then
        // b) create the failure event (it is done by SelRelPlan)
        if (e.isInternal() || C.hasListener() || ag.getPL().hasCandidatePlan(e.trigger)) {
            C.addEvent(e);
            if (logger.isLoggable(Level.FINE)) logger.fine("Added event " + e+ ", events = "+C.getEvents());
        }
    }

    /** remove the top action and requeue the current intention */
    private void updateIntention(Intention i) {
        if (!i.isFinished()) {
            i.peek().removeCurrentStep();
            confP.C.addIntention(i);
        } else {
            logger.fine("trying to update a finished intention!");
        }
    }

    /** generate a failure event for an intention */
    public boolean generateGoalDeletion(Intention i, List<Term> failAnnots) throws JasonException {
        boolean failEventIsRelevant = false;
        IntendedMeans im = i.peek();
        // produce failure event
        Event failEvent = findEventForFailure(i, im.getTrigger());
        if (failEvent != null) {
            failEventIsRelevant = true;
        } else {
            failEvent = new Event(im.getTrigger().clone(), i);
        }
        Term bodyPart = im.getCurrentStep().getBodyTerm().capply(im.unif);
        setDefaultFailureAnnots(failEvent, bodyPart, failAnnots);

        if (im.isGoalAdd()) {
            // notify listener
            if (hasGoalListener())
                for (GoalListener gl: goalListeners) {
                    gl.goalFailed(im.getTrigger());
                    if (!failEventIsRelevant)
                        gl.goalFinished(im.getTrigger(), FinishStates.unachieved);
                }

            if (failEventIsRelevant) {
                confP.C.addEvent(failEvent);
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
            confP.C.addExternalEv(im.getTrigger());
        } else {
            logger.warning("Could not finish intention: " + i + "\tTrigger: " + failEvent.getTrigger());
        }
        return failEventIsRelevant;
    }

    // similar to the one above, but for an Event rather than intention
    private boolean generateGoalDeletionFromEvent(List<Term> failAnnots) throws JasonException {
        Event ev = conf.C.SE;
        if (ev == null) {
            logger.warning("** It was impossible to generate a goal deletion event because SE is null! " + conf.C);
            return false;
        }

        Trigger tevent = ev.trigger;
        boolean failEeventGenerated = false;
        if (tevent.isAddition() && tevent.isGoal()) {
            // notify listener
            if (hasGoalListener())
                for (GoalListener gl: goalListeners)
                    gl.goalFailed(tevent);

            // produce failure event
            Event failEvent = findEventForFailure(ev.intention, tevent);
            if (failEvent != null) {
                setDefaultFailureAnnots(failEvent, tevent.getLiteral(), failAnnots);
                confP.C.addEvent(failEvent);
                failEeventGenerated = true;
                //logger.warning("Generating goal deletion " + failEvent.getTrigger() + " from event: " + ev.getTrigger());
            } else {
                logger.warning("No fail event was generated for " + ev.getTrigger());
                if (ev.intention != null) {
                    ev.intention.fail(getC());
                    logger.warning("\n"+ev.intention);
                }
            }
        } else if (ev.isInternal()) {
            logger.warning("Could not finish intention:\n" + ev.intention);
        }
        // if "discard" is set, we are deleting the whole intention!
        // it is simply not going back to I nor anywhere else!
        else if (setts.requeue()) {
            confP.C.addEvent(ev);
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

    private static final Atom aNOCODE = new Atom("no_code");

    /** add default error annotations (error, error_msg, code, code_src, code_line) in the failure event */
    private static void setDefaultFailureAnnots(Event failEvent, Term body, List<Term> failAnnots) {
        // add default failure annots
        if (failAnnots == null)
            failAnnots = JasonException.createBasicErrorAnnots( JasonException.UNKNOW_ERROR, "");

        Literal eventLiteral = failEvent.getTrigger().getLiteral().forceFullLiteralImpl();
        eventLiteral.addAnnots(failAnnots);

        // add failure annots in the event related to the code source
        Literal bodyterm = aNOCODE;
        Term codesrc     = aNOCODE;
        Term codeline    = aNOCODE;
        if (body != null && body instanceof Literal) {
            bodyterm = (Literal)body;
            if (bodyterm.getSrcInfo() != null) {
                if (bodyterm.getSrcInfo().getSrcFile() != null)
                    codesrc = new StringTermImpl(bodyterm.getSrcInfo().getSrcFile());
                codeline = new NumberTermImpl(bodyterm.getSrcInfo().getSrcLine());
            }
        }

        // code
        if (eventLiteral.getAnnot("code") == null)
            eventLiteral.addAnnot(ASSyntax.createStructure("code", bodyterm.copy().makeVarsAnnon()));

        // ASL source
        if (eventLiteral.getAnnot("code_src") == null)
            eventLiteral.addAnnot(ASSyntax.createStructure("code_src", codesrc));

        // line in the source
        if (eventLiteral.getAnnot("code_line") == null)
            eventLiteral.addAnnot(ASSyntax.createStructure("code_line", codeline));
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

        Agent.getScheduler().schedule(new Runnable() {
            public void run() {
                runAtBeginOfNextCycle(new Runnable() {
                    public void run() {
                        boolean drop = false;
                        if (intention == null) { // deadline in !!g, test if the agent still desires it
                            drop = desire.allDesires(C, body, new Unifier()).hasNext();
                        } else if (intention.size() >= isize && intention.hasTrigger(evt.getTrigger(), new Unifier())) {
                            drop = true;
                        }
                        if (drop) {
                            try {
                                FailWithDeadline ia = new FailWithDeadline(intention, evt.getTrigger());
                                ia.drop(TransitionSystem.this, body, new Unifier());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                getUserAgArch().wakeUpSense();
            }
        }, deadline, TimeUnit.MILLISECONDS);
    }

    class FailWithDeadline extends fail_goal {
        Intention intToDrop;
        Trigger   te;

        public FailWithDeadline(Intention i, Trigger t) {
            intToDrop = i;
            te        = t;
        }

        /* returns: >0 the intention was changed
         *           1 = intention must continue running
         *           2 = fail event was generated and added in C.E
         *           3 = simply removed without event
         */
        @Override
        public int dropIntention(Intention i, Trigger g, TransitionSystem ts, Unifier un) throws JasonException {
            if (i != null) {
                // only consider dropping if the intention is the one that created the deadline goal
                if (intToDrop == null) {
                    if (te != i.getBottom().getTrigger()) { // no intention, then consider the bottom trigger
                        return 0;
                    }
                } else if (!intToDrop.equals(i)) {
                    return 0;
                }

                if (i.dropGoal(g, un)) {
                    // notify listener
                    if (ts.hasGoalListener())
                        for (GoalListener gl: ts.getGoalListeners())
                            gl.goalFailed(g);

                    // generate failure event
                    Event failEvent = ts.findEventForFailure(i, g); // find fail event for the goal just dropped
                    if (failEvent != null) {
                        failEvent.getTrigger().getLiteral().addAnnots(JasonException.createBasicErrorAnnots("deadline_reached", ""));
                        ts.getC().addEvent(failEvent);
                        ts.getLogger().fine("'hard_deadline("+g+")' is generating a goal deletion event: " + failEvent.getTrigger());
                        return 2;
                    } else { // i is finished or without failure plan
                        ts.getLogger().fine("'hard_deadline("+g+")' is removing the intention without event:\n" + i);
                        return 3;
                    }
                }
            }
            return 0;
        }
    }

    public boolean canSleep() {
        return    (C.isAtomicIntentionSuspended() && !C.hasFeedbackAction() && !conf.C.hasMsg())  // atomic case
                  || (!conf.C.hasEvent() &&    // other cases (deliberate)
                      !conf.C.hasIntention() && !conf.C.hasFeedbackAction() && // (action)
                      !conf.C.hasMsg() &&  // (sense)
                      taskForBeginOfCycle.isEmpty() &&
                      getUserAgArch().canSleep());
    }

    public boolean canSleepSense() {
        return !C.hasMsg() && getUserAgArch().canSleep();
    }

    public boolean canSleepDeliberate() {
        return !C.hasEvent() && taskForBeginOfCycle.isEmpty() && C.getSelectedEvent() == null && getUserAgArch().canSleep();
    }

    public boolean canSleepAct() {
        //&& !C.hasPendingAction()
        return !C.hasIntention() && !C.hasFeedbackAction() && C.getSelectedIntention() == null && getUserAgArch().canSleep();
    }

    /**
     * Schedule a task to be executed in the begin of the next reasoning cycle.
     * It is used mostly to change the C only by the TS thread (e.g. by .wait)
     */
    public void runAtBeginOfNextCycle(Runnable r) {
        taskForBeginOfCycle.offer(r);
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
            if (logger.isLoggable(Level.FINE)) logger.fine("Start new reasoning cycle");
            getUserAgArch().reasoningCycleStarting();

            C.resetSense();

            if (nrcslbr >= setts.nrcbp()) {
                nrcslbr = 0;
                synchronized (C.syncApPlanSense) {
                    ag.buf(getUserAgArch().perceive());
                }
                getUserAgArch().checkMail();
            }
            nrcslbr++; // counting number of cycles since last belief revision

            // produce sleep events
            if (canSleep()) {
                if (!sleepingEvt) {
                    sleepingEvt = true;
                    if (ag.pl.getCandidatePlans(PlanLibrary.TE_JAG_SLEEPING) != null)
                        C.addExternalEv(PlanLibrary.TE_JAG_SLEEPING);
                } else {
                    //getUserAgArch().sleep(); // removes from here. sleep is in the archs
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

            stepSense = State.StartRC;
            do {
                applySemanticRuleSense();
            } while (stepSense != State.SelEv && getUserAgArch().isRunning());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "*** ERROR in the transition system (sense). "+conf.C+"\nCreating a new C!", e);
            conf.C.create();
        }
    }

    public void deliberate() {
        try {
            C.resetDeliberate();

            // run tasks allocated to be performed in the begin of the cycle
            Runnable r = taskForBeginOfCycle.poll();
            while (r != null) {
                r.run(); //It is processed only things related to operations on goals/intentions resumed/suspended/finished It can be placed in the deliberate stage, but the problem is the sleep when the synchronous execution is adopted
                r = taskForBeginOfCycle.poll();
            }

            stepDeliberate = State.SelEv;
            do {
                applySemanticRuleDeliberate();
            } while (stepDeliberate != State.ProcAct && getUserAgArch().isRunning());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "*** ERROR in the transition system (deliberate). "+conf.C+"\nCreating a new C!", e);
            conf.C.create();
        }
    }

    public void act() {
        try {
            C.resetAct();

            stepAct = State.ProcAct;
            do {
                applySemanticRuleAct();
            } while (stepAct != State.StartRC && getUserAgArch().isRunning());


            ActionExec action = C.getAction();
            if (action != null) {
                C.addPendingAction(action);
                // We need to send a wrapper for FA to the user so that add method then calls C.addFA (which control atomic things)
                getUserAgArch().act(action); //, C.getFeedbackActionsWrapper());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "*** ERROR in the transition system (act). "+conf.C+"\nCreating a new C!", e);
            conf.C.create();
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

             // run tasks allocated to be performed in the begin of the cycle
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
             logger.log(Level.SEVERE, "*** ERROR in the transition system. "+conf.C+"\nCreating a new C!", e);
             conf.C.create();
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
    public AgArch getUserAgArch() {
        return agArch;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return "TS of agent "+getUserAgArch().getAgName();
    }
}
