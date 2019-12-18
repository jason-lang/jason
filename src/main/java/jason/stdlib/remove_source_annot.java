package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
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
        return un.unifies(((Literal)args[0]).noSource(), args[1] );
    }

}
