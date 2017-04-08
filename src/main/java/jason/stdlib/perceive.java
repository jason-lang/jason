package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.perceive</code></b>.

  <p>Description: forces the agent architecture to do perception of the
  environment immediately. It is normally used when the number of reasoning
  cycles before perception takes place was changed (this is normally at every
  cycle).

  <p>Example:<ul>

  <li> <code>.perceive</code>.</li>

  </ul>
*/
public class perceive extends DefaultInternalAction {
    @Override public int getMinArgs() {
        return 0;
    }
    @Override public int getMaxArgs() {
        return 0;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ts.getAg().buf(ts.getUserAgArch().perceive());
        return true;
    }
}
