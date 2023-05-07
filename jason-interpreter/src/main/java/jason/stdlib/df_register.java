package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesFactory;

/**
  <p>Internal action: <b><code>.df_register(S [,T])</code></b>.

  <p>Description: register the agent in the Directory Facilitator as a provider of service S of type T (see FIPA specification).
    An optional second argument can be used to define the type of the service.

  <p>Parameters:<ul>

  <li>- service (literal): the service the agent is registered.</li>
  <li>- type (string -- optional): the type of the service.</li>

  </ul>

  <p>Examples:<ul>
  <li> <code>.df_register("sell(book)")</code>: register the agent as a book seller.
  <li> <code>.df_deregister("sell(book)","book-trading")</code>: register the agent as a book seller of type "book-trading".
</ul>

  @see jason.stdlib.df_deregister
  @see jason.stdlib.df_search
  @see jason.stdlib.df_subscribe

 */
@Manual(
        literal=".df_register(service[,type])",
        hint="register the agent in the Directory Facilitator as a provider of the service and, optionally, the type",
        argsHint= {
                "the service the agent is to be registered",
                "the type of the service [optional]"
        },
        argsType= {
                "literal",
                "string"
        },
        examples= {
                ".df_register(\"sell(book)\"): register the agent as a book seller",
                ".df_register(\"sell(book)\",\"book-trading\"): register the agent as a book seller of type \"book-trading\"",
                ".df_register(\"expert(stocks)\",\"finantial-consultant\"): register the agent as an expert in stocks of type \"finantial-consultant\""
        },
        seeAlso= {
                "jason.stdlib.df_search",
                "jason.stdlib.df_deregister",
                "jason.stdlib.df_subscribe"
        }
    )
@SuppressWarnings("serial")
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
        return 2;
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        ts.getAgArch().getRuntimeServices().dfRegister(ts.getAgArch().getAgName(), getService(args), getType(args));
        return true;
    }

    protected String getService(Term[] args) {
        if (args[0].isString())
            return ((StringTerm)args[0]).getString();
        else
            return args[0].toString();
    }

    protected String getType(Term[] args) {
        if (args.length>1) {
                if (args[1].isString())
                    return ((StringTerm)args[1]).getString();
            else if (!args[1].isVar())
                    return args[1].toString();
        }
        return "jason-type";
    }
}
