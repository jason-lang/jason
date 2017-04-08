package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.List;

/**
  <p>Internal action: <b><code>.assertz</code></b>.

  <p>Description: adds a new belief as the "+>" operator. However, it can be used in prolog like rules.

  <p>Parameters:<ul>
  <li>+ belief (literal): the belief that will be added in the end of the belief base.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.assertz(p)</code>: adds <code>p</code> in the belief base.</li>
  </ul>

  @see jason.stdlib.asserta
  @see jason.stdlib.abolish

 */
public class assertz extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral())
            if (!args[0].isGround() && !args[0].isRule())
                throw JasonException.createWrongArgument(this,"first argument must be a ground literal (or rule).");
    }

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        List<Literal>[] result = ts.getAg().brf((Literal)args[0],null,null,true);
        if (result != null) { // really added something
            // generate events
            ts.updateEvents(result,null);
        }
        return true;
    }

}
