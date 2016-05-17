package jason.asSemantics;

import jason.asSyntax.Term;

/**
 * Common interface for all arithmetic functions
 * 
 * @author Jomi
 *
 */
public interface ArithFunction {

    /** returns the name of the function */
    public String getName();
    
    /** evaluates/computes the function based on the args */
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception ;
    
    /** returns true if a is a good number of arguments for the function */
    public boolean checkArity(int a);
    
    /** returns true if the arguments of the function can be unground (as in .count) */
    public boolean allowUngroundTerms();
    
}
