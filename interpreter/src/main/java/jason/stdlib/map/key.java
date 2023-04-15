package jason.stdlib.map;

import java.util.Iterator;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;


public class key extends put {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new key();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return getIterator(args[1], getMap(un,args).keys().iterator(), un);
    }

    protected Iterator<Unifier> getIterator(Term arg, final Iterator<Term> i, Unifier un) throws Exception {
        return new Iterator<Unifier>() {
            Unifier c = null; // the current response (which is an unifier)

            public boolean hasNext() {
                if (c == null) // the first call of hasNext should find the first response
                    find();
                return c != null;
            }

            public Unifier next() {
                if (c == null) find();
                Unifier b = c;
                find(); // find next response
                return b;
            }

            void find() {
                while (i.hasNext()) {
                    c = un.clone();
                    if (c.unifiesNoUndo(arg, i.next()))
                        return; // member found in the list, c is the current response
                }
                c = null; // no member is found,
            }

            public void remove() {}
        };
    }
}
