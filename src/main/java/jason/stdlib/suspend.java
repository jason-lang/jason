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
  <p>Internal action:
  <b><code>.suspend(<i>G</i>)</code></b>.

  <p>Description: suspend goals <i>G</i>, i.e., all intentions trying to achieve G will stop
  running until the internal action <code>.resume</code> change the state of those intentions.
  A literal <i>G</i>
  is a goal if there is a triggering event <code>+!G</code> in any plan within
  any intention in I, E, PI, or PA.
  <br/>
  The meta-event <code>^!G[state(suspended)]</code> is produced.

  <p>Examples:<ul>

  <li> <code>.suspend(go(1,3))</code>: suspends intentions to go to the location 1,3.
  <li> <code>.suspend</code>: suspends the current intention.

  </ul>

  @see jason.stdlib.suspended
  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.resume

 */
public class suspend extends DefaultInternalAction {

    boolean suspendIntention = false;
    public static final String SUSPENDED_INT      = "suspended-";
    public static final String SELF_SUSPENDED_INT = SUSPENDED_INT+"self-";

    @Override public int getMinArgs() {
        return 0;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length == 1 && !args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        suspendIntention = false;

        Circumstance C = ts.getC();

        if (args.length == 0) {
            // suspend the current intention
            Intention i = C.getSelectedIntention();
            suspendIntention = true;
            i.setSuspended(true);
            C.addPendingIntention(SELF_SUSPENDED_INT+i.getId(), i);
            return true;
        }

        // use the argument to select the intention to suspend.

        Trigger      g = new Trigger(TEOperator.add, TEType.achieve, (Literal)args[0]);

        // ** Must test in PA/PI first since some actions (as .suspend) put intention in PI

        // suspending from Pending Actions
        for (ActionExec a: C.getPendingActions().values()) {
            Intention i = a.getIntention();
            if (i.hasTrigger(g, un)) {
                i.setSuspended(true);
                C.addPendingIntention(SUSPENDED_INT+i.getId(), i);
            }
        }

        // suspending from Pending Intentions
        for (Intention i: C.getPendingIntentions().values()) {
            if (i.hasTrigger(g, un)) {
                i.setSuspended(true);
            }
        }

        Iterator<Intention> itint = C.getIntentionsPlusAtomic();
        while (itint.hasNext()) {
            Intention i = itint.next();
            if (i.hasTrigger(g, un)) {
                i.setSuspended(true);
                C.removeIntention(i);
                C.addPendingIntention(SUSPENDED_INT+i.getId(), i);
                //System.out.println("sus "+g+" from I "+i.getId()+" #"+C.getPendingIntentions().size());
            }
        }

        // suspending the current intention?
        Intention i = C.getSelectedIntention();
        if (i != null && i.hasTrigger(g, un)) {
            suspendIntention = true;
            i.setSuspended(true);
            C.addPendingIntention(SELF_SUSPENDED_INT+i.getId(), i);
        }

        // suspending G in Events
        int c = 0;
        Iterator<Event> ie = C.getEventsPlusAtomic();
        while (ie.hasNext()) {
            Event e = ie.next();
            i = e.getIntention();
            if (un.unifies(g, e.getTrigger()) || (i != null && i.hasTrigger(g, un))) {
                C.removeEvent(e);
                C.addPendingEvent(SUSPENDED_INT+e.getTrigger()+(c++), e);
                if (i != null)
                    i.setSuspended(true);
                //System.out.println("sus "+g+" from E "+e.getTrigger());
            }


            /*
            if ( i != null &&
                    (i.hasTrigger(g, un) ||       // the goal is in the i's stack of IM
                     un.unifies(g, e.getTrigger())  // the goal is the trigger of the event
                    )
                ) {
                i.setSuspended(true);
                C.removeEvent(e);
                C.addPendingIntention(k, i);
            } else if (i == Intention.EmptyInt && un.unifies(g, e.getTrigger())) { // the case of !!
                // creates an intention to suspend the "event"
                i = new Intention();
                i.push(new IntendedMeans(
                        new Option(
                                new Plan(null, e.getTrigger(), Literal.LTrue,
                                        new PlanBodyImpl(BodyType.achieveNF, e.getTrigger().getLiteral())),
                                new Unifier()),
                        e.getTrigger()));
                e.setIntention(i);
                i.setSuspended(true);
                C.removeEvent(e);
                C.addPendingIntention(k, i);
            }
            */
        }

        return true;
    }

    @Override
    public boolean suspendIntention() {
        return suspendIntention;
    }
}
