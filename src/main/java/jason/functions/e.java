package jason.functions;

import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.e</code></b>: encapsulates java Math.E.
  
  <p>Example:<ul>
  <li> <code>math.e</code>: returns 2.718.</li>
  </ul>
   
  @author Jomi 
*/
public class e extends DefaultArithFunction  {

    public String getName() {
        return "math.e";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        return Math.E;
    }

    @Override
    public boolean checkArity(int a) {
        return a == 0;
    }
    
}
