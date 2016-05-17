package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.ground</code></b>.

<p>Description: checks whether the argument is ground, i.e., it has no free
variables. Numbers, Strings, and Atoms are always ground.

<p>Parameters:<ul>
<li>+ argument (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.ground(b(10))</code>: true.
<li> <code>.ground(10)</code>: true.
<li> <code>.ground(X)</code>: false if X is free or bound to a term with free variables.
<li> <code>.ground(a(X))</code>: false if X is free or bound to a term with free variables.
<li> <code>.ground([a,b,c])</code>: true.
<li> <code>.ground([a,b,c(X)])</code>: false if X is free or bound to a term with free variables.
</ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure

*/
public class ground extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new ground();
        return singleton;
    }
    
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return args[0].isGround();
    }
}
