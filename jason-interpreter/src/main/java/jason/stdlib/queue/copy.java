package jason.stdlib.queue;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Term;


public class copy extends add {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new copy();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        var source = getQueue(un, args);
        Queue<Term> q;
        if (source instanceof PriorityBlockingQueue) {
            q = new PriorityBlockingQueue<Term>();
        } else {
            q = new ArrayDeque<Term>();
        }
        for (Term t: source)
            q.offer(t.clone());

        return un.unifies(args[1], new ObjectTermImpl(q) {
            @Override
            public ObjectTerm clone() { // do not clone
                return this;
            }
        });
    }
}
