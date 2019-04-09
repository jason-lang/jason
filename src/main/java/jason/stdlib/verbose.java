package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.runtime.Settings;


/**
  <p>Internal action: <b><code>.verbose</code></b>.

  <p>Description: change the verbosity level.

  <p>Parameters:<ul>

  <li>+value (number): values are 0 (minimal), 1 (normal) and 2 (debug).<br/>

  </ul>

  <p>Example:<ul>

  <li> <code>.verbose(2)</code>: start showing debug messages</li>
  <li> <code>.verbose(1)</code>: show 'normal' messages</li>

  </ul>

*/

public class verbose extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new verbose();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Settings stts = ts.getSettings();
        stts.setVerbose( (int)((NumberTerm)args[0]).solve() );
        ts.getAg().getLogger().setLevel(stts.logLevel());
        ts.getLogger().setLevel(stts.logLevel());
        return true;
    }
}
