package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Level;

import env.WorldModel;

import arch.MinerArch;

public class set_target extends DefaultInternalAction {
    
    Location oldTarget = null;
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            WorldModel model = ((MinerArch)ts.getUserAgArch()).getModel();
            if (model == null) {
                ts.getLogger().log(Level.SEVERE, "no model to get near_least_visited!");
            } else {
                if (oldTarget != null && model.inGrid(oldTarget)) {
                    model.remove(WorldModel.TARGET, oldTarget);
                }
                NumberTerm x = (NumberTerm)terms[0]; 
                NumberTerm y = (NumberTerm)terms[1];
                Location t = new Location((int)x.solve(), (int)y.solve());
                if (model.inGrid(t)) {
                    model.add(WorldModel.TARGET, t);
                    oldTarget = t;
                }
            }
            return true;
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "jia.set_target error: "+e, e);
        }
        return false;        
    }
}
