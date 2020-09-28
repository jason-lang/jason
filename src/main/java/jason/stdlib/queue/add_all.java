package jason.stdlib.queue;

import java.util.Queue;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;


public class add_all extends create {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new add_all();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Queue<Term> q = getQueue(un, args);
        for (Term t: (ListTerm)args[1]) {
            q.offer(t);
        }
        return true;
    }
}
