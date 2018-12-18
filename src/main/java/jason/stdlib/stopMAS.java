package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesInfraTier;

/**
  <p>Internal action: <b><code>.stopMAS</code></b>.

  <p>Description: aborts the execution of all agents in the multi-agent system
  (and any simulated environment too).

  <p>Example:<ul>

  <li> <code>.stopMAS</code>.</li>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.kill_agent
  @see jason.runtime.RuntimeServicesInfraTier
 */
public class stopMAS extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 0;
    }
    @Override public int getMaxArgs() {
        return 0;
    }

    @Override
    public boolean canBeUsedInContext() {
        return false;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        RuntimeServicesInfraTier rs = ts.getUserAgArch().getRuntimeServices();
        rs.stopMAS();
        return true;
    }
}
