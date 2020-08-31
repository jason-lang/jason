package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.util.Iterator;

/**
  <p>Internal action: <b><code>.namespace</code></b>.

  <p>Description: checks whether the argument is a namespace.

  <p>Parameter:<ul>
  <li>+ arg[0] (any term): the term to be checked.<br/>
  </ul>

  <p>Examples assuming the BB is currently
  {a(10),family::brother(bob),b(f,1)}:
  <ul>
  <li> <code>.namespace(family)</code>: true.
  <li> <code>.namespace(any_other)</code>: false.
  </ul>

  @see jason.stdlib.findall
  @see jason.stdlib.setof
  @see jason.stdlib.count
*/
@Manual(
        literal=".namespace(argument)",
        hint="checks whether the argument is a namespace",
        argsHint= {
                "the term to be checked"
        },
        argsType= {
                "term"
        },
        examples= {
                ".namespace(family), assuming the BB is {a(10),family::brother(bob),b(f,1)}: true",
                ".namespace(any_other), assuming the BB is {a(10),family::brother(bob),b(f,1)}: false"
        },
        seeAlso= {
                "jason.stdlib.findall",
                "jason.stdlib.setof",
                "jason.stdlib.count"
        }
    )
@SuppressWarnings("serial")
public class namespace extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isAtom() & !args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument must be an atom or variable.");
    }

    @Override public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);

        if (!args[0].isVar()) {
            return ts.getAg().getBB().getNameSpaces().contains(args[0]);
        } else {
            return new Iterator<Unifier>() {
                Iterator<Atom> i  = ts.getAg().getBB().getNameSpaces().iterator();
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
                        if (!n.unifiesNoUndo(args[0], i.next()))
                            next();
                    } else {
                        n = null;
                    }

                    return c;
                }

                public void remove() {}
            };
        }
    }

}
