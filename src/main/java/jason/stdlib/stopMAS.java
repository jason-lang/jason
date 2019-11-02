package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServices;

/**
  <p>Internal action: <b><code>.stopMAS</code></b>.

  <p>Description: aborts the execution of all agents in the multi-agent system
  (and any simulated environment too).

  <p>Example:<ul>

  <li> <code>.stopMAS</code>.</li>
  <li> <code>.stopMAS(2000)</code> shuts down the system in 2 seconds.
  The signal +jag_shutting_down(T) will be produced so that agents can prepare themselves for the shutdown.<br/>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.kill_agent
  @see jason.runtime.RuntimeServices
 */
@Manual(
        literal=".stopMAS",
        hint="aborts the execution of all agents in the multi-agent system (and any simulated environment too)",
        argsHint= {
                ""
        },
        argsType= {
                ""
        },
        examples= {
                ".stopMAS: close multi-agent system application"
        },
        seeAlso= {
                "jason.stdlib.create_agent",
                "jason.stdlib.kill_agent",
                "jason.runtime.RuntimeServices"
        }
    )
public class stopMAS extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 0;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeUsedInContext() {
        return false;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        RuntimeServices rs = ts.getUserAgArch().getRuntimeServices();
        int deadline = 0;
        if (args.length == 1 && args[0].isNumeric()) {
            deadline = (int)((NumberTerm)args[0]).solve();
        }
        rs.stopMAS(deadline);
        return true;
    }
}
