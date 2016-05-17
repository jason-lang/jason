package jason.functions;

import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
   <p>Function: <b><code>math.random(N)</code></b>: encapsulates java Math.random;
   If N is not informed: returns a value greater than or equal to 0.0 and less than 1.0;
   If N is informed: returns a value greater than or equal to 0.0 and less than N.
  
   <p>Examples:<ul>
   <li> <code>math.random</code>: returns the random number between 0 and 1.</li>
   <li> <code>math.random(10)</code>: returns the random number between 0 and 9.9999.</li>
   </ul>
   
   @author Jomi
  
   @see jason.functions.floor
   @see jason.functions.ceil
   
*/
public class Random extends DefaultArithFunction  {

    public String getName() {
        return "math.random";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args.length == 1 && args[0].isNumeric()) {
            return Math.random() * ((NumberTerm)args[0]).solve();
        } else {
            return Math.random();
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 0 || a == 1;
    }
    
}
