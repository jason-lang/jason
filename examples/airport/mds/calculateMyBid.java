package mds;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

public class calculateMyBid extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String id = ts.getUserAgArch().getAgName().substring(3);
        int bid = Integer.parseInt(id) * 10;
        // args[0] is the unattended luggage Report Number
        return un.unifies(args[1], new NumberTermImpl(bid));
    }
}
