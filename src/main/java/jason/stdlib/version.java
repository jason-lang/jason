package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.util.Config;

/**
  <p>Internal action: <b><code>.version(<i>V</i>)</code></b>.

  <p>Description: unifies <i>V</i> with the Jason version.

  <p>Parameter:<ul>

  <li>- version (string): the variable to receive the version<br/>
  </ul>


  </ul>

*/
@Manual(
        literal=".version(version)",
        hint="gets the Jason version",
        argsHint= {
                "the variable to receive the version"
        },
        argsType= {
                "string"
        },
        examples= {
                ".version(V): unifies V with, i.e., \"2.4-SNAPSHOT\""
        }
    )
@SuppressWarnings("serial")
public class version extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new version();
        return singleton;
    }


    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        return un.unifies(args[0], new StringTermImpl(Config.get().getJasonVersion()));
    }
}
