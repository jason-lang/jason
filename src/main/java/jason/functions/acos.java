package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.acos(N)</code></b>: encapsulates java Math.acos(N),
  returns the arc cosine of a value; the returned angle is in the range 0.0 through pi.
  
  @author Jomi 
*/
public class acos extends DefaultArithFunction  {

    public String getName() {
        return "math.acos";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            return Math.acos(((NumberTerm)args[0]).solve());
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
