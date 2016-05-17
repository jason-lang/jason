package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.list</code></b>.

  <p>Description: checks whether the argument is a list, e.g.: "[a,b]", "[]". 

  <p>Parameter:<ul>
  <li>+ argument (any term): the term to be checked.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.list([a,b,c])</code>: true.
  <li> <code>.list([a,b,c(X)])</code>: true.
  <li> <code>.list(b(10))</code>: false.
  <li> <code>.list(10)</code>: false.
  <li> <code>.list("home page")</code>: false.
  <li> <code>.list(X)</code>: false if X is free, true if X is bound to a list.
  <li> <code>.list(a(X))</code>: false.
  </ul>

  @see jason.stdlib.atom
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/
public class list extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new list();
        return singleton;
    }

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return args[0].isList();
    }
}
