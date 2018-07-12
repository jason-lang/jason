package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.Event;
import jason.asSemantics.GoalListener;
import jason.asSemantics.GoalListener.FinishStates;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;

/**
  <p>Internal action:
  <b><code>.fail_goal(<i>G</i>)</code></b>.

  <p>Description: aborts goals <i>G</i> in the agent circumstance as if a plan
  for such goal had failed. An event <code>-!G</code> is generated.  
  A literal <i>G</i>
  is a goal if there is a triggering event <code>+!G</code> in any plan within
  any intention; also note that intentions can be suspended hence appearing
  in sets E, PA, or PI of the agent's circumstance as well.
  <br/>
  The meta-event <code>^!G[state(failed)]</code> is produced.

  <p>Example:<ul>

  <li> <code>.fail_goal(go(1,3))</code>: aborts an attempt to achieve
  goals such as <code>!go(1,3)</code> as if a plan for it had failed, the
  generated event is <code>-!go(1,3)</code>.

  </ul>

  (Note: this internal action was introduced in a DALT 2006 paper, where it was called .dropGoal(G,false).)

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

 */
public class fail_goal extends succeed_goal {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        drop(ts, (Literal)args[0], un);
        return true;
    }

    /* returns: >0 the intention was changed
     *           1 = intention must continue running
     *           2 = fail event was generated and added in C.E
     *           3 = simply removed without event
     */
    @Override
    public int dropIntention(Intention i, Trigger g, TransitionSystem ts, Unifier un) throws JasonException {
        if (i != null) {
            if (i.dropGoal(g, un)) {
                // notify listener
                if (ts.hasGoalListener())
                    for (GoalListener gl: ts.getGoalListeners())
                        gl.goalFailed(g);

                // generate failure event
                Event failEvent = ts.findEventForFailure(i, g); // find fail event for the goal just dropped
                if (failEvent != null) {
                	failEvent = new Event(failEvent.getTrigger().capply(un),failEvent.getIntention());
                    ts.getC().addEvent(failEvent);
                    ts.getLogger().fine("'.fail_goal("+g+")' is generating a goal deletion event: " + failEvent.getTrigger());
                    return 2;
                } else { // i is finished or without failure plan
                    ts.getLogger().fine("'.fail_goal("+g+")' is removing the intention without event:\n" + i);
                    if (ts.hasGoalListener())
                        for (GoalListener gl: ts.getGoalListeners())
                            gl.goalFinished(g, FinishStates.unachieved);

                    i.fail(ts.getC());
                    return 3;
                }
            }
        }
        return 0;
    }

    @Override
    void dropInEvent(TransitionSystem ts, Event e, Intention i) throws Exception {
        e.getTrigger().setTrigOp(TEOperator.del);
    }
}
