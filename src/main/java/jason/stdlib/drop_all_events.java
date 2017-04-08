package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.drop_all_events</code></b>.

  <p>Description: removes all desires that the
     agent has not yet committed to.
     No event is produced.

  <p>This action changes the agent's circumstance structure by simply
    emptying the whole set of events (E). This action is complementary
    to <code>.drop_all_desires</code> and <code>.drop_all_intentions</code>,
    in case all entries are to be removed from the set of events but
    <b>not</b> from the set of intentions.

  <p>Example:<ul>

  <li> <code>.drop_all_events</code>.

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
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
public class drop_all_events extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 0;
    }
    @Override public int getMaxArgs() {
        return 0;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ts.getC().clearEvents();
        ts.getC().clearPendingEvents();
        return true;
    }
}
