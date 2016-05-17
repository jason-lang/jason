// Internal action code for project FoodSimulation

package math;

import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

import java.util.logging.Logger;

/** function that computes the distance between two number */
public class distance extends DefaultArithFunction {

    private Logger logger = Logger.getLogger("FoodSimulation."+distance.class.getName());

    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        try {
            int n1 = (int)((NumberTerm)args[0]).solve();
            int n2 = (int)((NumberTerm)args[1]).solve();
            return Math.abs(n1 - n2);
        } catch (Exception e) {
            logger.warning("Error in function 'math.distance'! "+e);
        }
        return 0;
    }
}
