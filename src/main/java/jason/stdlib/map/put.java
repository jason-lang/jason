package jason.stdlib.map;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.MapTerm;
import jason.asSyntax.MapTermImpl;
import jason.asSyntax.Term;


public class put extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new put();
        return singleton;
    }

    protected MapTerm getMap(Unifier un, Term[] args) {
        if (args[0].isMap()) {
            return (MapTerm)args[0];
        } else if (args[0].isVar()) {
            MapTerm s = new MapTermImpl();
            un.unifies(args[0], s);
            return s;
        }
        return null;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        getMap(un,args).put(args[1], args[2]);
        return true;
    }
}
