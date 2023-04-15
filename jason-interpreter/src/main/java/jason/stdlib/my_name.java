package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.my_name</code></b>.

  <p>Description: gets the agent's unique identification in the
  multi-agent system. This identification is given by the runtime
  infrastructure of the system (local, saci, jade, ...).

  <p>Parameter:<ul>

  <li>+/- name (atom or variable): if this is a variable, unifies the agent
  name and the variable; if it is an atom, succeeds if the atom is equal to
  the agent's name.<br/>

  </ul>

  <p>Example:<ul>

  <li> <code>.my_name(N)</code>: unifies <code>N</code> with the
  agent's name.</li>
  <li> <code>.my_name(bob)</code>: true if the agent's name is \"bob\".</li>

  </ul>

  @see jason.stdlib.send
  @see jason.stdlib.broadcast

  @see jason.stdlib.all_names

 */
@Manual(
        literal=".my_name(result)",
        hint="gets the agent's unique identification in the multi-agent system",
        argsHint= {
                "unifies the agent name"
        },
        argsType= {
                "atom or variable"
        },
        examples= {
                ".my_name(N): unifies N with the agent's name",
                ".my_name(bob): true if the agent's name is \"bob\""
        },
        seeAlso= {
                "jason.stdlib.broadcast",
                "jason.stdlib.send",
                "jason.stdlib.all_names"
        }
    )
@SuppressWarnings("serial")
public class my_name extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new my_name();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return un.unifies(args[0], new Atom(ts.getAgArch().getAgName()));
    }
}
