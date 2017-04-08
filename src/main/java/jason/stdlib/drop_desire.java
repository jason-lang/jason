package jason.stdlib;

import jason.asSemantics.Circumstance;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;


/**
  <p>Internal action: <b><code>.drop_desire(<i>D</i>)</code></b>.

  <p>Description: removes desire <i>D</i> from the agent circumstance.
  This internal action simply removes all <i>+!D</i> entries
  (those for which <code>.desire(D)</code> would succeed) from both
  the set of events and the set of intentions.
  No event is produced as a consequence of dropping desires.

  <p>Example:<ul>

  <li> <code>.drop_desire(go(X,3))</code>: remove desires such as
  <code>&lt;+!go(1,3),_&gt;</code> from the set of events and
  intentions having plans with triggering events such as
  <code>+!go(1,3)<code>.

  </ul>

  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_event
  @see jason.stdlib.drop_intention
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.intend
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended


 */
public class drop_desire extends drop_intention {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        dropEvt(ts.getC(), (Literal)args[0], un);
        dropInt(ts.getC(), (Literal)args[0], un);
        return true;
    }

    public void dropEvt(Circumstance C, Literal l, Unifier un) {
        Trigger te = new Trigger(TEOperator.add, TEType.achieve, l);

        // search in E
        C.removeEvents(te, un);
        //dropEvt(te, un, C.getEventsPlusAtomic());

        // search in PE (only the event need to be checked, the related intention is handled by dropInt)
        C.removePendingEvents(te, un);
        //dropEvt(te, un, C.getPendingEvents().values().iterator());
    }

    /* moved to circumstance
    private static void dropEvt(Trigger te, Unifier un, Iterator<Event> ie) {
        while (ie.hasNext()) {
            Event  ei = ie.next();
            Trigger t = ei.getTrigger();
            if (ei.getIntention() != Intention.EmptyInt) { // since the unifier of the intention will not be used, apply it to the event before comparing to the event to be dropped
                t = t.capply(ei.getIntention().peek().getUnif());
            }
            if (un.clone().unifiesNoUndo(te, t)) {
                ie.remove();
            }
        }
    }*/
}
