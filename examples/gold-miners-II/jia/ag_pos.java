package jia;

import java.util.logging.Level;

import env.*;
import arch.MinerArch;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.environment.grid.Location;

/** gets the location of some agent (this info is in the world model) */
public class ag_pos extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            WorldModel model = ((MinerArch)ts.getUserAgArch()).getModel();

            int agId = MinerArch.getAgId(terms[0].toString());
            Location l = model.getAgPos(agId);
            if (l != null) {
                return un.unifies(terms[1], new NumberTermImpl(l.x)) &&
                       un.unifies(terms[2], new NumberTermImpl(l.y));
            }
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "add_fatigue error: "+e, e);
        }
        return false;
    }
}

