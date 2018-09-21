package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.df_search(S,L)</code></b>.

  <p>Description: unifies in L a list of all agents providing the service S (see FIPA Directory Facilitator specification).

  <p>Examples:<ul>
  <li> <code>.df_search(sell(book),L)</code>: unifies L with [bob,alice] in case these two agents have registered as book sellers.
  </ul>


  @see jason.stdlib.df_register
  @see jason.stdlib.df_deregister
  @see jason.stdlib.df_subscribe

 */
public class df_search extends DefaultInternalAction {

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
        return 2;
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ListTerm lt = new ListTermImpl();
        for (String a: ts.getUserAgArch().getRuntimeServices().dfSearch((Literal)args[0])) {
            lt.add(new Atom(a));
        }        
        return un.unifies(args[1], lt);
    }
}
