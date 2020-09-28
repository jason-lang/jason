package jason.stdlib.queue;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;


public class head extends create {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new head();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Term t = getQueue(un, args).peek();
        if (t == null) {
            return false;
        } else {
            return un.unifies(args[1], t);
        }
    }
}
