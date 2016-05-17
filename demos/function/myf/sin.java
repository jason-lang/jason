// User defined function for project function.mas2j

package myf;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class sin extends DefaultArithFunction {

    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            double n = ((NumberTerm)args[0]).solve(); // get the first argument
            return Math.sin(n);
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

}

