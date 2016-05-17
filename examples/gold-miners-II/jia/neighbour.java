package jia;

import java.util.logging.Level;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class neighbour extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            int iagx = (int) ((NumberTerm) terms[0]).solve();
            int iagy = (int) ((NumberTerm) terms[1]).solve();
            int itox = (int) ((NumberTerm) terms[2]).solve();
            int itoy = (int) ((NumberTerm) terms[3]).solve();
            return new Location(iagx, iagy).isNeigbour(new Location(itox, itoy));
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "neighbour error: "+e, e);         
        }
        return false;
    }
}
