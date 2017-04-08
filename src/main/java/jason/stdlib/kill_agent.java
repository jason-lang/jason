package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
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

  </ul>

  <p>Example:<ul>

  <li> <code>.kill_agent(bob)</code>: kills the agent named bob.</li>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.save_agent
  @see jason.stdlib.stopMAS
  @see jason.runtime.RuntimeServicesInfraTier

*/
public class kill_agent extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        String name;
        if (args[0].isString())
            name = ((StringTerm)args[0]).getString();
        else
            name = args[0].toString();
        return ts.getUserAgArch().getRuntimeServices().killAgent(name, ts.getUserAgArch().getAgName());
    }
}
