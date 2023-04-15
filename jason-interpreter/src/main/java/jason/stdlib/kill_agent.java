package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesFactory;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;

/**
  <p>Internal action: <b><code>.kill_agent</code></b>.

  <p>Description: kills the agent whose name is given as parameter. This is a
     provisional internal action to be used while more adequate mechanisms for
     creating and killing agents are being developed. In particular, note that
     an agent can kill any other agent, without any consideration on
     permissions, etc.! It is the programmers' responsibility to use this
     action.


  <p>Parameters:<ul>

  <li>+ name (atom or string): the name of the agent to be killed.<br/>
  <li>+ deadline (number): the given time for the agent finish some intentions before being killed. The signal +jag_shutting_down(T) will be produced so that the agent can prepare itself for the shutdown.<br/>

  </ul>

  <p>Example:<ul>

  <li> <code>.kill_agent(bob)</code>: kills the agent named bob.</li>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.save_agent
  @see jason.stdlib.stopMAS
  @see jason.runtime.RuntimeServices

*/
@Manual(
		literal=".kill_agent(name)",
		hint="kills the agent whose name is given as parameter",
		argsHint= {
				"the name of the agent to be killed"
		},
		argsType= {
				"atom or string"
		},
		examples= {
				".kill_agent(bob): kills the agent named bob"
		},
		seeAlso= {
				"jason.stdlib.create_agent",
				"jason.stdlib.save_agent",
				"jason.stdlib.stopMAS",
				"jason.runtime.RuntimeServices"
		}
	)
@SuppressWarnings("serial")
public class kill_agent extends DefaultInternalAction {

	@Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        String name;
        if (args[0].isString())
            name = ((StringTerm)args[0]).getString();
        else
            name = args[0].toString();
        int deadline = 0;
        if (args.length == 2)
        	deadline = (int)((NumberTerm)args[1]).solve();
        return RuntimeServicesFactory.get().killAgent(name, ts.getAgArch().getAgName(), deadline);
    }
}
