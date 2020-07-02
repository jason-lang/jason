package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

/**
  <p>Internal action: <b><code>.drop_intention(<i>I</i>)</code></b>.

  <p>Description: removes intentions to achieve goal <i>I</i> from the set of
  intentions of the agent (suspended intentions are also considered).
  No event is produced.

  <p>Parameters:<ul>

  <li>- goal (literal): the goal the intentions achieve.</li>

  </ul>

  <p>Example:<ul>

  <li> <code>.drop_intention(go(1,3))</code>: removes intentions having a plan
  with triggering event <code>+!go(1,3)</code> in the agent's current circumstance.

  <li> <code>.drop_intention</code>: removes the current intention.

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

 */
@Manual(
		literal=".drop_intention(goal)",
		hint="removes intentions to achieve the referred goal",
		argsHint= {
				"the goal the intentions achieve"
		},
		argsType= {
				"literal"
		},
		examples= {
				".drop_intention(go(1,3)): removes intentions having a plan with triggering event +!go(1,3) in the agent's current circumstance."
		},
		seeAlso= {
				"jason.stdlib.intend",
				"jason.stdlib.desire",
				"jason.stdlib.drop_all_desires",
				"jason.stdlib.drop_all_events",
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
public class drop_intention extends DefaultInternalAction {

	@Override public int getMinArgs() {
        return 0;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length > 0 && !args[0].isLiteral() && !args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument '"+args[0]+"' must be a literal or variable");
    }

    private boolean resultSuspend = false;

    @Override
    public boolean suspendIntention() {
		return resultSuspend;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        resultSuspend = false;
        if (args.length == 0) {
    		resultSuspend = true; // to drop the current intention
        } else {
        	resultSuspend = dropInt(ts.getC(),(Literal)args[0],un); // to drop the current intention
        }
        return true;
    }

    /**
     * Drops an intention based on a goal argument
     *
     * returns true if the current intention is dropped
     */
    public boolean dropInt(Circumstance C, Literal goal, Unifier un) {
        Unifier bak = un.clone();
        Trigger g = new Trigger(TEOperator.add, TEType.achieve, goal);
        boolean isCurrentInt = false;
        Iterator<Intention> iint = C.getAllIntentions();
        while (iint.hasNext()) { // for all intentions
    		Intention i = iint.next();
            if (i.hasTrigger(g, un)) { // if the intention i has goal g
                C.dropIntention(i);
                isCurrentInt = isCurrentInt || i.equals(C.getSelectedIntention());
                un = bak.clone();
            }
        }

        return isCurrentInt;
        /*

        // intention may be suspended in E or PE
        Iterator<Event> ie = C.getEventsPlusAtomic();
        while (ie.hasNext()) {
            Event e = ie.next();
            Intention i = e.getIntention();
            if (i != null && i.hasTrigger(g, un)) {
                C.removeEvent(e);
                un = bak.clone();
            }
        }
        for (String k: C.getPendingEvents().keySet()) {
            Intention i = C.getPendingEvents().get(k).getIntention();
            if (i != null && i.hasTrigger(g, un)) {
                C.removePendingEvent(k);
                un = bak.clone();
            }
        }

        // intention may be suspended in PA! (in the new semantics)
        for (ActionExec a: C.getPendingActions().values()) {
            Intention i = a.getIntention();
            if (i.hasTrigger(g, un)) {
                C.dropPendingAction(i);
                un = bak.clone();
            }
        }

        Iterator<Intention> itint = C.getRunningIntentionsPlusAtomic();
        while (itint.hasNext()) {
            Intention i = itint.next();
            if (i.hasTrigger(g, un)) {
                C.dropRunningIntention(i);
                un = bak.clone();
            }
        }

        // intention may be suspended in PI! (in the new semantics)
        for (Intention i: C.getPendingIntentions().values()) {
            if (i.hasTrigger(g, un)) {
                C.dropPendingIntention(i);
                un = bak.clone();
            }
        }
        */
    }

}
