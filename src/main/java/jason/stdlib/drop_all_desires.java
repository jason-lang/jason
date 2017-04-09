package jason.stdlib;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.drop_all_desires</code></b>.

  <p>Description: removes all desires of the agent. No event is
  produced.

  <p>This action changes the agent's circumstance structure by simply emptying
  the whole set of events (E) and then calling
  <code>.drop_all_intentions</code>.

  <p>Example:<ul>

  <li> <code>.drop_all_desires</code>.

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
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
public class drop_all_desires extends drop_all_intentions {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        super.execute(ts, un, args);
        ts.getC().clearEvents();
        ts.getC().clearPendingEvents();
        return true;
    }
}
