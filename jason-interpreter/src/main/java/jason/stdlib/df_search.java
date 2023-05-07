package jason.stdlib;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesFactory;

/**
  <p>Internal action: <b><code>.df_search(S [,T] , L)</code></b>.

  <p>Description: unifies in L a list of all agents providing the service S of type T (see FIPA Directory Facilitator specification).
    An optional second argument can be used to define the type of the service.

  <p>Parameters:<ul>

  <li>- service (literal): the service the agents are registered.</li>
  <li>- type (string -- optional): the type of the service.</li>
  <li>- type (list): the resultant list of agents providing the service.</li>

  </ul>

  <p>Examples:<ul>
  <li> <code>.df_search("sell(book)",L)</code>: unifies L with [bob,alice] in case these two agents have registered as book sellers.
  </ul>


  @see jason.stdlib.df_register
  @see jason.stdlib.df_deregister
  @see jason.stdlib.df_subscribe

 */
@Manual(
        literal=".df_search(service[,type],result)",
        hint="list of all agents providing the service and, optionally, the type",
        argsHint= {
                "the service the agents are registered",
                "the type of the service [optional]",
                "the resulting list of agents providing the service"
        },
        argsType= {
                "literal",
                "string",
                "list"
        },
        examples= {
                ".df_search(\"sell(book)\",L): unifies L with [bob,alice] in case these two agents have registered as book sellers"
        },
        seeAlso= {
                "jason.stdlib.df_register",
                "jason.stdlib.df_deregister",
                "jason.stdlib.df_subscribe"
        }
    )
@SuppressWarnings("serial")
public class df_search extends df_register {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new df_search();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ListTerm lt = new ListTermImpl();
        for (String a: ts.getAgArch().getRuntimeServices().dfSearch(getService(args), getType(args))) {
            lt.add(new Atom(a));
        }
        return un.unifies(args[args.length-1], lt);
    }
}
