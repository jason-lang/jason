package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.abs(N)</code></b>: encapsulates java Math.abs(N).
  
  <p>Examples:<ul>
  <li> <code>math.abs(1)</code>: returns 1.</li>
  <li> <code>math.abs(-1)</code>: returns 1.</li>
  </ul>
   
  @author Jomi 
*/
public class Abs extends DefaultArithFunction  {

    public String getName() {
        return "math.abs";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            double n = ((NumberTerm)args[0]).solve();
            return Math.abs(n);
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
