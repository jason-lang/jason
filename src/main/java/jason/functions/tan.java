package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.tan(N)</code></b>: encapsulates java Math.tan(N),
  returns the trigonometric tangent of an angle.
  
  @author Jomi 
*/
public class tan extends DefaultArithFunction  {

    public String getName() {
        return "math.tan";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            return Math.tan(((NumberTerm)args[0]).solve());
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
