package jason.stdlib.set;

import java.util.Collection;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.SetTerm;
import jason.asSyntax.Term;


public class add_all extends add {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new add_all();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        SetTerm s = getSet(un, args);
        for (Term t: (Collection<Term>)args[1]) {
            s.add(t);
        }
        return true;
    }
}
