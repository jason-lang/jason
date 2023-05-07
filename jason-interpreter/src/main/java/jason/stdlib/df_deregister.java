package jason.stdlib;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesFactory;

/**
  <p>Internal action: <b><code>.df_deregister(S [,T])</code></b>.

  <p>Description: removes the agent in the Directory Facilitator as a provider of service S of type T (see FIPA specification).
    An optional second argument can be used to define the type of the service.

  <p>Parameters:<ul>

  <li>- service (literal): the service the agent is registered.</li>
  <li>- type (string -- optional): the type of the service.</li>

  </ul>

  <p>Examples:<ul>
  <li> <code>.df_deregister("sell(book)")</code>: deregister the agent as a book seller.
  </ul>

  @see jason.stdlib.df_register
  @see jason.stdlib.df_search
  @see jason.stdlib.df_subscribe

 */
@Manual(
        literal=".df_deregister(service[,type])",
        hint="removes the agent in the Directory Facilitator as a provider of the service and, optionally, the type",
        argsHint= {
                "the service the agent is registered",
                "the type of the service [optional]"
        },
        argsType= {
                "literal",
                "string"
        },
        examples= {
                ".df_deregister(\"sell(book)\"): deregister the agent as a book seller",
                ".df_deregister(\"expert(stocks)\",\"finantial-consultant\"): deregister the agent as an expert in stocks of type \"finantial-consultant\""
        },
        seeAlso= {
                "jason.stdlib.df_register",
                "jason.stdlib.df_search",
                "jason.stdlib.df_subscribe"
        }
    )
@SuppressWarnings("serial")
public class df_deregister extends df_register {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new df_deregister();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ts.getAgArch().getRuntimeServices().dfDeRegister(ts.getAgArch().getAgName(), getService(args), getType(args));
        return true;
    }
}
