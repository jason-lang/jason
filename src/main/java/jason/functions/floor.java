package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.floor(N)</code></b>: encapsulates java Math.floor(N),
  returns the largest double value that is not greater than the argument and is 
  equal to a mathematical integer.
  
  <p>Examples:<ul>
  <li> <code>math.floor(3.1)</code>: returns 3.</li>
  <li> <code>math.floor(3.9)</code>: returns 3.</li>
  </ul>
   
  @author Jomi
   
  @see jason.functions.Random
  @see jason.functions.ceil
  
*/
public class floor extends DefaultArithFunction  {

    public String getName() {
        return "math.floor";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            double n = ((NumberTerm)args[0]).solve();
            return Math.floor(n);
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
