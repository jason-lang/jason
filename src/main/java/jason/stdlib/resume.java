package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.Circumstance;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.GoalListener;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

/**
  <p>Internal action:
  <b><code>.resume(<i>G</i>)</code></b>.

  <p>Description: resume goals <i>G</i> that were suspended by <code>.suspend</code>.
  <br/>
  The meta-event <code>^!G[state(resumed)]</code> is produced.

  <p>Example:<ul>

  <li> <code>.resume(go(1,3))</code>: resume the goal of going to location 1,3.

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended

 */
@Manual(
        literal=".resume(goals)",
        hint="resume goals that were suspended by .suspend (produces the meta-event ^!goals[state(resumed)])",
        argsHint= {
                "the goal to be resume"
        },
        argsType= {
                "literal"
        },
        examples= {
                ".resume(go(1,3)): resume the goal of going to location 1,3"
        },
        seeAlso= {
                "jason.stdlib.intend",
                "jason.stdlib.desire",
                "jason.stdlib.drop_all_desires",
                "jason.stdlib.drop_all_events",
                "jason.stdlib.drop_all_intentions",
                "jason.stdlib.drop_intention",
                "jason.stdlib.drop_desire",
                "jason.stdlib.succeed_goal",
                "jason.stdlib.fail_goal",
                "jason.stdlib.intention",
                "jason.stdlib.resume",
                "jason.stdlib.suspend",
                "jason.stdlib.suspended"
        }
    )
@SuppressWarnings("serial")
public class resume extends DefaultInternalAction {

    private static Term resumeReason = ASSyntax.createAtom("resume_ia");

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

        Trigger      g = new Trigger(TEOperator.add, TEType.achieve, (Literal)args[0]);
        Circumstance C = ts.getC();

        // Search the goal in PI
        Iterator<String> ik = C.getPendingIntentions().keySet().iterator();
        while (ik.hasNext()) {
            String k = ik.next();
            Intention i = C.getPendingIntentions().get(k);
            if (i.isSuspended() && i.hasTrigger(g, un)) {
                i.setSuspended(false);

                // notify meta event listeners
                if (C.hasListener())
                    for (CircumstanceListener el : C.getListeners())
                        el.intentionResumed(i, resumeReason);

                if (k.startsWith(suspend.SUSPENDED_INT)) { // if not SUSPENDED_INT, it was suspended while already in PI, so, do not remove it from PI, just change the suspended status
                    ik.remove();

                    // add it back in I if not in PA
                    if (! C.getPendingActions().containsKey(i.getId())) {
                        C.resumeIntention(i, null);
                    }
                } else if (C.hasListener()) {
                    for (CircumstanceListener el : C.getListeners())
                        el.intentionWaiting(i, i.getSuspendedReason());
                }

                // remove the IA .suspend in case of self-suspend
                if (k.startsWith(suspend.SELF_SUSPENDED_INT))
                    i.peek().removeCurrentStep();
            }
        }

        // Search the goal in PE
        ik = C.getPendingEvents().keySet().iterator();
        while (ik.hasNext()) {
            String k = ik.next();
            if (k.startsWith(suspend.SUSPENDED_INT)) {
                Event e = C.getPendingEvents().get(k);
                Intention i = e.getIntention();
                if (un.unifies(g, e.getTrigger()) || (i != null && i.hasTrigger(g, un))) {
                    ik.remove();

                    // notify meta event listeners
                    if (i == null) {
                        if (ts.hasGoalListener())
                            for (GoalListener gl : ts.getGoalListeners())
                                gl.goalResumed(e.getTrigger(), resumeReason);
                    } else if (C.hasListener()) {
                        for (CircumstanceListener el : C.getListeners())
                            el.intentionResumed(i, resumeReason);
                    }

                    C.addEvent(e);
                    if (i != null)
                        i.setSuspended(false);
                }
            }
        }
        return true;
    }

}
