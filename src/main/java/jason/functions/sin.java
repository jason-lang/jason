package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.sin(N)</code></b>: encapsulates java Math.sin(N),
  returns the trigonometric sine of an angle.
  
  @author Jomi 
*/
public class sin extends DefaultArithFunction  {

    public String getName() {
        return "math.sin";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            return Math.sin(((NumberTerm)args[0]).solve());
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
