package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;

@SuppressWarnings("serial")
public class remove_source_annot extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new remove_source_annot();
        return singleton;
    }
    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (args[0].isList()) {
            ListTerm res = new ListTermImpl();
            for (Term t: (ListTerm)args[0]) {
                res.add( processTerm( t) );
            }
            return un.unifies( res, args[1] );
        } else {
            return un.unifies( processTerm( args[0] ), args[1] );
        }
    }

    Term processTerm(Term in) {
        if (in instanceof Plan) {
            Plan p = (Plan)in.clone();
            if (p.getLabel() != null)
                p.getLabel().delSources();
            return p;
        }
        if (in.isLiteral()) {
            return ((Literal)in).forceFullLiteralImpl().noSource();
        }
        return in;
    }

}
