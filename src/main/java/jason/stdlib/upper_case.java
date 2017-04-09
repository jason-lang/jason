
package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.upper_case(S1,S2)</code></b>.

  <p>Description: converts the string S1 into upper case S2.

  <p>Parameters:<ul>
  <li>+ S1 (a term). The term representation as a string will be used.<br/>
  <li>-/+ S2 (a string).<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.upper_case("CArtAgO",X)</code>: unifies X with "CARTAGO".
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.reverse

*/
public class upper_case extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new upper_case();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        String arg = null;
        if (args[0].isString())
            arg = ((StringTerm)args[0]).getString();
        else
            arg = args[0].toString();
        arg = arg.toUpperCase();
        return un.unifies(new StringTermImpl(arg), args[1]);
    }
}

