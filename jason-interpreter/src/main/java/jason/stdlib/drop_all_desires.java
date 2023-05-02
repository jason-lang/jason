package jason.stdlib;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.drop_all_desires</code></b>.

  <p>Description: removes all desires of the agent. No event is
  produced.

  <p>This action changes the agent's circumstance structure (1) by removing
  from set of events (E) all events for goals (like +!g -!g +?g) and then (2) calling
  <code>.drop_all_intentions</code>.
  It does not remove external events (which are not considered as a desire)

  <p>Example:<ul>

  <li> <code>.drop_all_desires</code>: events and all intentions are dropped.

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

 */
@Manual(
		literal=".drop_all_desires",
		hint="removes all desires of the agent",
		argsHint= {
				""
		},
		argsType= {
				""
		},
		examples= {
				".drop_all_desires: events and all intentions are dropped"
		},
		seeAlso= {
				"jason.stdlib.intend",
				"jason.stdlib.desire",
				"jason.stdlib.drop_all_events",
				"jason.stdlib.drop_all_intentions",
				"jason.stdlib.drop_event",
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
public class drop_all_desires extends drop_all_intentions {

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        super.execute(ts, un, args);
        ts.getC().clearEvents(true);
        ts.getC().clearPendingEvents();
        return true;
    }
}
