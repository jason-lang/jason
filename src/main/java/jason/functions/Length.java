package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>.length(L)</code></b>: returns the size of either the list or string L.

  <p>Examples:<ul>
  <li> <code>.length("aa")</code>: returns 2.</li>
  <li> <code>.length([a,b,c])</code>: returns 3.</li>
  </ul>
   
  @see jason.stdlib.length internal action version
   
  @author Jomi 
*/
public class Length extends DefaultArithFunction  {

    public String getName() {
        return ".length";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws JasonException {
        if (args[0].isList()) {
            return ((ListTerm)args[0]).size();
        } else if (args[0].isString()) {
            return ((StringTerm)args[0]).getString().length();
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not a list or a string!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
    /*
    @Override
    public boolean allowUngroundTerms() {
        return true;
    }
    */
}
