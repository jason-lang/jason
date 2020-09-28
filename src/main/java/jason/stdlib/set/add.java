package jason.stdlib.set;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.SetTerm;
import jason.asSyntax.SetTermImpl;
import jason.asSyntax.Term;


public class add extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new add();
        return singleton;
    }

    protected SetTerm getSet(Unifier un, Term[] args) {
        if (args[0].isSet()) {
            return (SetTerm)args[0];
        } else if (args[0].isVar()) {
            SetTerm s = new SetTermImpl();
            un.unifies(args[0], s);
            return s;
        }
        return null;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        getSet(un,args).add(args[1]);
        return true;
    }
}
