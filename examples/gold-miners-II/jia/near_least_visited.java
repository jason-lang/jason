package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Level;

import arch.LocalWorldModel;
import arch.MinerArch;

/**
 * Gets the near least visited location.
 * Its is based on the agent's model of the world.
 * 
 * @author jomi
 *
 */
public class near_least_visited extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            LocalWorldModel model = ((MinerArch)ts.getUserAgArch()).getModel();
            if (model == null) {
                ts.getLogger().log(Level.SEVERE, "no model to get near_least_visited!");
            } else {
                NumberTerm agx = (NumberTerm)terms[0]; 
                NumberTerm agy = (NumberTerm)terms[1];
                Location n = model.getNearLeastVisited((int)agx.solve(), (int)agy.solve());
                un.unifies(terms[2], new NumberTermImpl(n.x));
                un.unifies(terms[3], new NumberTermImpl(n.y));
                //ts.getLogger().info("at "+agx+","+agy+" to "+n.x+","+n.y);
                return true;
            }
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "near_least_visited error: "+e, e);
        }
        return false;
    }
}
