package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.string</code></b>.

  <p>Description: checks whether the argument is a string, e.g.: "a". 

  <p>Parameter:<ul>
  <li>+ arg[0] (any term): the term to be checked.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.string("home page")</code>: true.
  <li> <code>.string(b(10))</code>: false.
  <li> <code>.string(b)</code>: false.
  <li> <code>.string(X)</code>: false if X is free, true if X is bound to a string.
  </ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/
public class string extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new string();
        return singleton;
    }

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return args[0].isString();
    }
}
