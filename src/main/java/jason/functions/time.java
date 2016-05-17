package jason.functions;

import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>system.time</code></b>: encapsulates java System.currentTimeMillis(),
  returns the current time in milliseconds.
  
  @see jason.stdlib.time internal action time

  @author Jomi 
*/
public class time extends DefaultArithFunction  {

    public String getName() {
        return "system.time";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        return System.currentTimeMillis();
    }

    @Override
    public boolean checkArity(int a) {
        return a == 0;
    }
    
}
