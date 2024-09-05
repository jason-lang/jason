package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.util.Iterator;

/**
  <p>Internal action: <b><code>.namespace_get_prop</code></b>.

  <p>Description: gets a property for a namespace.

  <p>Parameter:<ul>
  <li>+ arg[0] (atom): the namespace id.<br/>
  <li>+ arg[1] (atom): the key.<br/>
  <li>- arg[2] (var or term): the value.<br/>
  <li>+ (optional) arg[3] (term): default value if key does not exit.<br/>
  </ul>

  <p>Example:
  <ul>
  <li> <code>.namespace_get_prop(family,uri,X)</code>: X unifies with the value for key uri; internal action fails if no such key.
  <li> <code>.namespace_get_prop(family,uri,X,"http://x.com")</code>: X unifies with the value for key uri; unifies with "http://x.com" if no key.
  <li> <code>.namespace_get_prop(family,K)</code>: unifies K with any property key (backtracks for all keys).
  <li> <code>.namespace_get_prop(family,K,V)</code>: unifies K with any property jey and its value V.
  </ul>

  @see namespace
  @see namespace_set_prop
*/

public class namespace_get_prop extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 4;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isAtom())
            throw JasonException.createWrongArgument(this,"first argument must be an atom (the namespace id).");
    }

    @Override public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        if (!args[1].isVar()) {
            var t = ts.getAg().getBB().getNameSpaceProp((Atom) args[0], (Atom) args[1]);
            if (t != null) {
                return un.unifies(t, args[2]);
            } else if (args.length == 4) {
                return un.unifies(args[3], args[2]);
            }
        } else { // gets all props
            return new Iterator<Unifier>() {
                Iterator<Atom> i  = ts.getAg().getBB().getNameSpaceProps((Atom)args[0]).iterator();
                Unifier        n  = null;

                {
                    next(); // consume the first (and set first n value, i.e. the first solution)
                }

                public boolean hasNext() {
                    return n != null;
                }

                public Unifier next() {
                    Unifier c = n;

                    n = un.clone();
                    if (i.hasNext()) {
                        var key = i.next();
                        if (!n.unifiesNoUndo(args[1], key)) {
                            next();
                        } else {
                            if (args.length == 3) {
                                var value = ts.getAg().getBB().getNameSpaceProp((Atom)args[0], key);
                                n.unifiesNoUndo(args[2], value);
                            }
                        }
                    } else {
                        n = null;
                    }

                    return c;
                }

                public void remove() {}
            };
        }
        return true;

    }

}
