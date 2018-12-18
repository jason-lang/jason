package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;


/**
  <p>Internal action: <b><code>.intend(<i>G</i>, [ <i>I</i> ] )</code></b>.

  <p>Description: checks if goal <i>G</i> is intended: <i>G</i> is intended 
  if there is a triggering event <code>+!G</code> in any plan within an
  intention <i>I</i>; just note that intentions can appear in E (list of events), PA (intentions with pending actions),
  and PI (intentions waiting for something) as well. 
  This internal action backtracks all values for G.

  <p>Example:<ul>

  <li> <code>.intend(go(1,3))</code>: is true if a plan with triggering event
  <code>+!go(1,3)</code> appears in an intention of the agent.
  <li> <code>.intend(go(1,3),I)</code>: as above and <code>I</code> unifies with the intention that contains the goal. 
  <code>I</code> is a representation of the intention as a term @see jason.stdlib.current_intention.
  

  </ul>

  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

 */
public class intend extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral() && !args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument must be a literal or variable");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return allIntentions(ts.getC(),(Literal)args[0],args.length == 2 ? args[1] : null, un);
    }

    /*
    public boolean intends(Circumstance C, Literal l, Unifier un) {
        Trigger g = new Trigger(TEOperator.add, TEType.achieve, l);

        // we need to check the intention in the selected event in this cycle!!!
        // (as it was already removed from E)
        if (C.getSelectedEvent() != null) {
            // logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SE);
            if (C.getSelectedEvent().getIntention() != null)
                if (C.getSelectedEvent().getIntention().hasTrigger(g, un))
                    return true;
        }

        // we need to check the selected intention in this cycle too!!!
        if (C.getSelectedIntention() != null) {
            // logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SI);
            if (C.getSelectedIntention().hasTrigger(g, un))
                return true;
        }

        // intention may be in E
        for (Event evt : C.getEvents()) {
            if (evt.getIntention() != null && evt.getIntention().hasTrigger(g, un))
                return true;
        }

        // intention may be suspended in PE
        for (Event evt : C.getPendingEvents().values()) {
            if (evt.getIntention() != null && evt.getIntention().hasTrigger(g, un))
                return true;
        }

        // intention may be suspended in PA! (in the new semantics)
        if (C.hasPendingAction()) {
            for (ActionExec ac: C.getPendingActions().values()) {
                if (ac.getIntention().hasTrigger(g, un))
                    return true;
            }
        }

        // intention may be suspended in PI! (in the new semantics)
        if (C.hasPendingIntention()) {
            for (Intention intention: C.getPendingIntentions().values()) {
                if (intention.hasTrigger(g, un))
                    return true;
            }
        }

        for (Intention i : C.getIntentions()) {
            if (i.hasTrigger(g, un))
                return true;
        }

        return false;
    }
     */

    // data structures where intentions can be found
    enum Step { selEvt, selInt, evt, pendEvt, pendAct, pendInt, intentions, end }

    //private static Logger logger = Logger.getLogger(intend.class.getName());

    public static Iterator<Unifier> allIntentions(final Circumstance C, final Literal l, final Term intAsTerm, final Unifier un) {
        final Trigger g = new Trigger(TEOperator.add, TEType.achieve, l);

        return new Iterator<Unifier>() {
            Step curStep = Step.selEvt;
            Unifier solution = null; // the current response (which is an unifier)
            Intention curInt = null; // the intention of sulution
            Iterator<Event>      evtIterator     = null;
            Iterator<Event>      pendEvtIterator = null;
            Iterator<ActionExec> pendActIterator = null;
            Iterator<Intention>  pendIntIterator = null;
            Iterator<Intention>  intInterator    = null;

            public boolean hasNext() {
                if (solution == null) // the first call of hasNext should find the first response
                    find();
                return solution != null;
            }

            public Unifier next() {
                if (solution == null) find();
                Unifier b = solution;
                find(); // find next response
                return b;
            }
            public void remove() {}

            boolean isSolution() {
                if (curInt != null) {
                    solution = un.clone();
                    if (curInt.hasTrigger(g, solution)) {
                        if (intAsTerm != null) {
                            return solution.unifies(intAsTerm, curInt.getAsTerm());
                        } else {
                            return true;
                        }
                    }
                }
                return false;
            }

            void find() {
                switch (curStep) {

                case selEvt:
                    curStep = Step.selInt; // set next step
                    // we need to check the intention in the selected event in this cycle!!!
                    // (as it was already removed from E)
                    if (C.getSelectedEvent() != null) {
                        // logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SE);
                        curInt = C.getSelectedEvent().getIntention();
                        if (isSolution())
                            return;
                    }
                    find();
                    return;

                case selInt:
                    curStep = Step.evt; // set next step
                    // we need to check the selected intention in this cycle too!!!
                    curInt = C.getSelectedIntention();
                    if (isSolution())
                        return;
                    find();
                    return;

                case evt:
                    if (evtIterator == null)
                        evtIterator = C.getEventsPlusAtomic();

                    if (evtIterator.hasNext()) {
                        curInt = evtIterator.next().getIntention();
                        if (isSolution())
                            return;
                    } else {
                        curStep = Step.pendEvt; // set next step
                    }
                    find();
                    return;

                case pendEvt:
                    if (pendEvtIterator == null)
                        pendEvtIterator = C.getPendingEvents().values().iterator();

                    if (pendEvtIterator.hasNext()) {
                        curInt = pendEvtIterator.next().getIntention();
                        if (isSolution())
                            return;
                    } else {
                        curStep = Step.pendAct; // set next step
                    }
                    find();
                    return;

                case pendAct:
                    // intention may be suspended in PA! (in the new semantics)
                    if (C.hasPendingAction()) {
                        if (pendActIterator == null)
                            pendActIterator = C.getPendingActions().values().iterator();

                        if (pendActIterator.hasNext()) {
                            curInt = pendActIterator.next().getIntention();
                            if (isSolution())
                                return;
                        } else {
                            curStep = Step.pendInt; // set next step
                        }
                    } else {
                        curStep = Step.pendInt; // set next step
                    }
                    find();
                    return;

                case pendInt:
                    // intention may be suspended in PI! (in the new semantics)
                    if (C.hasPendingIntention()) {
                        if (pendIntIterator == null)
                            pendIntIterator = C.getPendingIntentions().values().iterator();

                        if (pendIntIterator.hasNext()) {
                            curInt   = pendIntIterator.next();
                            if (isSolution())
                                return;
                        } else {
                            curStep = Step.intentions; // set next step
                        }
                    } else {
                        curStep = Step.intentions; // set next step
                    }
                    find();
                    return;

                case intentions:
                    if (intInterator == null)
                        intInterator = C.getIntentionsPlusAtomic();

                    if (intInterator.hasNext()) {
                        curInt = intInterator.next();
                        if (isSolution())
                            return;
                    } else {
                        curStep = Step.end; // set next step
                    }
                    find();
                    return;

                case end:

                }
                solution = null; // nothing found
            }
        };
    }

}
