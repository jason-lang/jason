package jason.stdlib.queue;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Term;


public class create extends DefaultInternalAction {

    @SuppressWarnings("unchecked")
    protected Queue<Term> getQueue(Unifier un, Term[] args) {
        if (args[0] instanceof ObjectTerm) {
            return (Queue<Term>)((ObjectTerm)args[0]).getObject();
        } else {
            return null;
        }
    }

    protected boolean createQueue(Unifier un, Term[] args) {
        if (args[0].isVar()) {
            Queue<Term> q;
            boolean priority = args.length == 2 && args[1].toString().equals("priority");
            if (priority) {
                q = new PriorityBlockingQueue<Term>();
            } else {
                q = new ArrayDeque<Term>();
            }
            return un.unifies(args[0], new ObjectTermImpl(q) {
                @Override
                public ObjectTerm clone() { // do not clone
                    return this;
                }
            });
        } else {
            return false;
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return createQueue(un, args);
    }
}
