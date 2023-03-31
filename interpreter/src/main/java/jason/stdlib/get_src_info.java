package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanBody;
import jason.asSyntax.Term;

/**

 used by test lib

*/

public class get_src_info extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new get_src_info();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        if (args[0].isLiteral()) {
            Literal l;
            if (args[0].isPlanBody()) {
                l = (Literal)((PlanBody)args[0]).getBodyTerm();
            } else {
                l = ((Literal)args[0]);
            }
            l = (Literal)l.capply(un);
            l.addSourceInfoAsAnnots( l.getSrcInfo() );
            return un.unifies(args[1], l);
        }
        return false;
    }
}
