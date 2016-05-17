package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.asin(N)</code></b>: encapsulates java Math.asin(N),
  returns the arc sine of a value; the returned angle is in the range -pi/2 through pi/2.
  
  @author Jomi 
*/
public class asin extends DefaultArithFunction  {

    public String getName() {
        return "math.asin";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            return Math.asin(((NumberTerm)args[0]).solve());
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
