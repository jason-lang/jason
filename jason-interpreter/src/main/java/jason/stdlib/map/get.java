package jason.stdlib.map;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;


public class get extends put {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new get();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Term v = getMap(un,args).get(args[1]);
        if (v == null) {
            if (args.length == 4)
                return un.unifies(args[2], args[3]);
            else
                return false;
        } else {
            return un.unifies(args[2], v);
        }
    }
}
