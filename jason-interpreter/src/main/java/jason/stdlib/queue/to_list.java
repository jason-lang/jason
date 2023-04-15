package jason.stdlib.queue;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;


public class to_list extends create {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new to_list();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        ListTerm result = new ListTermImpl();
        ListTerm tail = result;
        for (Term t: getQueue(un, args))
            tail = tail.append(t.clone());
        return un.unifies(args[1], result);
    }
}
