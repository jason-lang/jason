package jason.asSemantics;

import jason.asSyntax.Term;

import java.io.Serializable;

/**
 * 
 * Useful default implementation of all methods of ArithFunction interface.
 * 
 * @author Jomi
 *
 */
public abstract class DefaultArithFunction implements ArithFunction, Serializable {

    public String getName() {
        return getClass().getName();
    }
    
    public boolean checkArity(int a) {
        return true;
    }

    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        return 0;
    }
    
    public boolean allowUngroundTerms() {
        return false;
    }

    @Override
    public String toString() {
        return "function "+getName();
    }

}
