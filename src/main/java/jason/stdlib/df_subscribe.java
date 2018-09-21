package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.df_subscribe(S)</code></b>.

  <p>Description: subscribes the agent as interested in providers of service S.
  For each new agent providing this service, the agent will receive a message <tell provider(Ag,Service)>.

  <p>Examples:<ul>
  <li> <code>.df_subscribe(sell(book))</code>: subscribe the agent for providers of agents that sell books.
  </ul>


  @see jason.stdlib.df_register
  @see jason.stdlib.df_deregister
  @see jason.stdlib.df_subscribe

 */
public class df_subscribe extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new df_subscribe();
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
        ts.getUserAgArch().getRuntimeServices().dfSubscribe(ts.getUserAgArch().getAgName(), (Literal)args[0]);
        return true;        
    }
}
