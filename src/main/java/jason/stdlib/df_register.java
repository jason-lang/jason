package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.df_register(S)</code></b>.

  <p>Description: register the agent in the Directory Facilitator as a provider of service S (see FIPA specification).

  <p>Examples:<ul>
  <li> <code>.df_register(sell(book))</code>: register the agent as a book seller.
  </ul>

  @see jason.stdlib.df_deregister
  @see jason.stdlib.df_search
  @see jason.stdlib.df_subscribe

 */
public class df_register extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new df_register();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);        
        ts.getUserAgArch().getRuntimeServices().dfRegister(ts.getUserAgArch().getAgName(), (Literal)args[0]);
        return true;
    }
}
