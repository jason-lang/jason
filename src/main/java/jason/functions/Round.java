package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.round(N)</code></b>: encapsulates java Math.round(N);
  returns the closest integer to the argument.
  
  <p>Examples:<ul>
  <li> <code>math.round(1.1)</code>: returns 1.</li>
  <li> <code>math.round(1.9)</code>: returns 2.</li>
  </ul>
   
  @author Jomi 
*/
public class Round extends DefaultArithFunction  {

    public String getName() {
        return "math.round";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            double n = ((NumberTerm)args[0]).solve();
            return Math.round(n);
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
