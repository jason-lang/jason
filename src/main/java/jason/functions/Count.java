package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;

import java.util.Iterator;

/** 
  <p>Function: <b><code>.count(B)</code></b>: counts the number of occurrences of a particular belief
  (pattern) in the agent's belief base, as the internal action .count.

  <p>Example:<ul> 

  <li> <code>.count(a(2,_))</code>: returns the number of beliefs
  that unify with <code>a(2,_)</code>.</li>
  
  </ul>
  
  @see jason.stdlib.count internal action version

  @author Jomi 
*/
public class Count extends DefaultArithFunction  {

	public String getName() {
	    return ".count";
	}
	
	@Override
	public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
	    if (ts == null) {
            throw new JasonException("The TransitionSystem parameter of the function '.count' cannot be null.");
	    }
        LogicalFormula logExpr = (LogicalFormula)args[0];
        int n = 0;
        Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), new Unifier());
        while (iu.hasNext()) {
            iu.next();
            n++;
        }
        return n;
	}

	@Override
	public boolean checkArity(int a) {
		return a == 1;
	}
	
	@Override
	public boolean allowUngroundTerms() {
	    return true;
	}
	
}
