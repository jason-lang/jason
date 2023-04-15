package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesFactory;

/**
  <p>Internal action: <b><code>.all_names</code></b>.

  <p>Description: get the names of all agents in the system.
  This identification is given by the runtime
  infrastructure of the system (local, saci, jade, ...)

  <p>Parameters:<ul>
  <li>+/- names (list): this argument unifies with a list of all agents in the system.<br/>
  </ul>

  <p>Examples:<ul>

  <li> <code>.all_names(L)</code>: unifies with L a list of all agents in the system.</li>

  </ul>

  @see jason.stdlib.my_name
  @see jason.runtime.RuntimeServices
*/
@Manual(
        literal=".all_names(result)",
        hint="get the names of all agents in the system",
        argsHint= {
                "the resulting unification with all agent's names"
        },
        argsType= {
                "list"
        },
        examples= {
                ".all_names(L): unifies with L a list of all agents in the system"
        },
        seeAlso= {
                "jason.stdlib.broadcast",
                "jason.stdlib.send",
                "jason.stdlib.all_names",
                "jason.runtime.RuntimeServices"
        }
    )
@SuppressWarnings("serial")
public class all_names extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ListTerm ln = new ListTermImpl();
        ListTerm tail = ln;
        for (String a: RuntimeServicesFactory.get().getAgentsNames()) {
            tail = tail.append(new Atom(a));
        }
        return un.unifies(args[0], ln);
    }
}
