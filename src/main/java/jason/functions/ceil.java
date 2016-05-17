package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.ceil(N)</code></b>: encapsulates java Math.ceil(N),
  returns the smallest double value that is not less than the argument and is 
  equal to a mathematical integer.
  
  <p>Examples:<ul>
  <li> <code>math.ceil(3.1)</code>: returns 4.</li>
  <li> <code>math.ceil(3.9)</code>: returns 4.</li>
  </ul>
   
  @author Jomi 
  
   @see jason.functions.floor
   @see jason.functions.Random
  
*/
public class ceil extends DefaultArithFunction  {

    public String getName() {
        return "math.ceil";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            double n = ((NumberTerm)args[0]).solve();
            return Math.ceil(n);
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
