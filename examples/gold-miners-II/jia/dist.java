package jia;

import java.util.logging.Level;

import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.environment.grid.Location;

/**
 * Gets the manhattan distance between two points.
 *
 * @author jomi
 */
public class dist extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            int iagx = (int) ((NumberTerm) terms[0]).solve();
            int iagy = (int) ((NumberTerm) terms[1]).solve();
            int itox = (int) ((NumberTerm) terms[2]).solve();
            int itoy = (int) ((NumberTerm) terms[3]).solve();
            int dist = new Location(iagx, iagy).distance(new Location(itox, itoy));
            return un.unifies(terms[4], new NumberTermImpl(dist));
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "dist error: "+e, e);
        }
        return false;
    }
}
