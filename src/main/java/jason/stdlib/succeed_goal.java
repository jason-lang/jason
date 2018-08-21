package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.GoalListener;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSemantics.GoalListener.FinishStates;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

import java.util.Iterator;

/**
  <p>Internal action:
  <b><code>.succeed_goal(<i>G</i>)</code></b>.

  <p>Description: remove goals <i>G</i> from the agent circumstance as if a plan
  for such goal had successfully finished. <i>G</i>
  is a goal if there is a triggering event <code>+!G</code> in any plan within any
  intention; also note that intentions can be suspended hence appearing
  in E, PA, or PI as well.
  <br/>
  The meta-event <code>^!G[state(finished)]</code> is produced.

  <p>Example:<ul>

  <li> <code>.succeed_goal(go(X,3))</code>: stops any attempt to achieve goals such as
  <code>!go(1,3)</code> as if it had already been achieved.

  </ul>

  (Note: this internal action was introduced in a DALT 2006 paper, where it was called .dropGoal(G,true).)

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.fail_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

 */
public class succeed_goal extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        drop(ts, (Literal)args[0], un);
        return true;
    }

    public void drop(TransitionSystem ts, Literal l, Unifier un) throws Exception {
        Trigger g = new Trigger(TEOperator.add, TEType.achieve, l);
        Circumstance C = ts.getC();
        Unifier bak = un.clone();

        Iterator<Intention> itint = C.getIntentionsPlusAtomic();
        while (itint.hasNext()) {
            Intention i = itint.next();
            if (dropIntention(i, g, ts, un) > 1) {
                C.dropIntention(i);
                un = bak.clone();
            }
        }

        // dropping the current intention?
        dropIntention(C.getSelectedIntention(), g, ts, un);
        un = bak.clone();

        // dropping G in Events
        Iterator<Event> ie = C.getEventsPlusAtomic();
        while (ie.hasNext()) {
            Event e = ie.next();
            // test in the intention
            Intention i = e.getIntention();
            int r = dropIntention(i, g, ts, un);
            if (r > 0) {
                C.removeEvent(e);
                if (r == 1) {
                    C.resumeIntention(i);
                }
                un = bak.clone();
            } else {
                // test in the event
                Trigger t = e.getTrigger();
                if (i != Intention.EmptyInt && !i.isFinished()) {
                    t = t.capply(i.peek().getUnif());
                }
                if (un.unifies(g, t)) {
                    dropInEvent(ts,e,i);
                    un = bak.clone();
                }
            }
        }

        // dropping G in Pending Events
        for (String ek: C.getPendingEvents().keySet()) {
            // test in the intention
            Event e = C.getPendingEvents().get(ek);
            Intention i = e.getIntention();
            int r = dropIntention(i, g, ts, un);
            if (r > 0) {
                C.removePendingEvent(ek);
                if (r == 1) {
                    C.resumeIntention(i);
                }
                un = bak.clone();
            } else {
                // test in the event
                Trigger t = e.getTrigger();
                if (i != Intention.EmptyInt && !i.isFinished()) { //i.size() > 0) {
                    t = t.capply(i.peek().getUnif());
                }
                if (un.unifies(g, t)) {
                    dropInEvent(ts,e,i);
                    un = bak.clone();
                }
            }
        }

        // dropping from Pending Actions
        for (ActionExec a: C.getPendingActions().values()) {
            Intention i = a.getIntention();
            int r = dropIntention(i, g, ts, un);
            if (r > 0) { // i was changed
                C.removePendingAction(i.getId());  // remove i from PA
                if (r == 1) {                      // i must continue running
                    C.resumeIntention(i);          // and put the intention back in I
                }                                  // if r > 1, the event was generated and i will be back soon
                un = bak.clone();
            }
        }

        // dropping from Pending Intentions
        for (Intention i: C.getPendingIntentions().values()) {
            int r = dropIntention(i, g, ts, un);
            if (r > 0) {
                C.removePendingIntention(i.getId());
                if (r == 1) {
                    C.resumeIntention(i);
                }
                un = bak.clone();
            }
        }
    }

    /* returns: >0 the intention was changed
     *           1 = intention must continue running
     *           2 = fail event was generated and added in C.E
     *           3 = simply removed without event
     */
    public int dropIntention(Intention i, Trigger g, TransitionSystem ts, Unifier un) throws JasonException {
        if (i != null && i.dropGoal(g, un)) {
            if (ts.hasGoalListener())
                for (GoalListener gl: ts.getGoalListeners())
                    gl.goalFinished(g, FinishStates.achieved);

            // continue the intention
            if (!i.isFinished()) { // could be finished after i.dropGoal() !!
                if (ts.getC().getSelectedIntention() != i) // if i is not the current intention, remove
                    i.peek().removeCurrentStep();
                ts.applyClrInt(i);
                return 1;
            } else {
                ts.applyClrInt(i);
                return 3;
            }
        }
        return 0;
    }

    void dropInEvent(TransitionSystem ts, Event e, Intention i) throws Exception {
        Circumstance C = ts.getC();
        C.removeEvent(e);
        if (i != null) {
            if (ts.hasGoalListener())
                for (GoalListener gl: ts.getGoalListeners())
                    gl.goalFinished(e.getTrigger(), FinishStates.achieved);
            i.peek().removeCurrentStep();
            ts.applyClrInt(i);
            C.addIntention(i);
        }
    }
}
