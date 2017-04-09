package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.util.Iterator;

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
