package jason.functions;

import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.pi</code></b>: encapsulates java Math.PI.
  
  <p>Example:<ul>
  <li> <code>math.pi</code>: returns 3.14.</li>
  </ul>
   
  @author Jomi 
*/
public class pi extends DefaultArithFunction  {

    public String getName() {
        return "math.pi";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        return Math.PI;
    }

    @Override
    public boolean checkArity(int a) {
        return a == 0;
    }
    
}
