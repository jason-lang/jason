package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.namespace_set_prop</code></b>.

  <p>Description: sets a property for a namespace.

  <p>Parameter:<ul>
  <li>+ arg[0] (atom): the namespace id.<br/>
  <li>+ arg[1] (atom): the key.<br/>
  <li>+ arg[2] (term): the value.<br/>
  </ul>

  <p>Example:
  <ul>
  <li> <code>.namespace_set_prop(family,uri,"http://hubner.org")</code>: true.
  </ul>

  @see namespace
  @see namespace_get_prop
*/

public class namespace_set_prop extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 3;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isAtom())
            throw JasonException.createWrongArgument(this,"first argument must be an atom (the namespace id).");
    }

    @Override public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        ts.getAg().getBB().setNameSpaceProp( (Atom)args[0], (Atom) args[1], args[2]);
        return true;
    }

}
