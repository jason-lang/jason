package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesFactory;

/**
  <p>Internal action: <b><code>.stopMAS</code></b>.

  <p>Description: aborts the execution of all agents in the multi-agent system
  (and any simulated environment too).

  <p>Example:<ul>

  <li> <code>.stopMAS</code>.</li>
  <li> <code>.stopMAS(2000)</code> shuts down the system in 2 seconds.
  The signal +jag_shutting_down(T) will be produced so that agents can prepare themselves for the shutdown.<br/>
  <li> <code>.stopMAS(2000,false)</code> same as before, but do not kill the JVM.
  <li> <code>.stopMAS(0,1)</code> shuts down the system and returns 1.

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
        return 2;
    }

    @Override
    public boolean canBeUsedInContext() {
        return false;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        int deadline = 0;
        int exitValue = 0;
        if (args.length >= 1 && args[0].isNumeric()) {
            deadline = (int)((NumberTerm)args[0]).solve();
        }
        if (args.length >= 2 && args[1].isNumeric()) {
            exitValue = (int)((NumberTerm)args[1]).solve();
        }
        boolean stopJVM = !(args.length >= 2 && args[1].toString().equals("false"));
        RuntimeServicesFactory.get().stopMAS(deadline, stopJVM, exitValue);
        return true;
    }
}
