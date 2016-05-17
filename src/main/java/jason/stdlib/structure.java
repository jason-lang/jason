package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.structure</code></b>.

  <p>Description: checks whether the argument is a structure, e.g.: "p", "p(1)",
  "[a,b]".  Numbers, strings and free variables are not structures.

  <p>Parameter:<ul>
  <li>+ argument (any term): the term to be checked.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.structure(b(10))</code>: true.
  <li> <code>.structure(b)</code>: true.
  <li> <code>.structure(10)</code>: false.
  <li> <code>.structure("home page")</code>: false.
  <li> <code>.structure(X)</code>: false if X is free, true if X is bound to a structure.
  <li> <code>.structure(a(X))</code>: true.
  <li> <code>.structure([a,b,c])</code>: true.
  <li> <code>.structure([a,b,c(X)])</code>: true.
  </ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.ground

*/
public class structure extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new structure();
        return singleton;
    }

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return args[0].isStructure();
    }
}
