package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
<p>Function: <b><code>math.min(N1,N2)</code></b>: encapsulates java Math.min(N1,N2).
   It also returns the min number of a list.

<p>Examples:<ul>
<li> <code>math.min(1,3)</code>: returns 1.</li>
<li> <code>math.min([3,a,"s",5])</code>: returns 3.</li>
</ul>
 
@author Jomi 

@see jason.functions.Max
*/
public class Min extends DefaultArithFunction  {

    public String getName() {
        return "math.min";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isNumeric() && args[1].isNumeric()) {
            double n0 = ((NumberTerm)args[0]).solve();
            double n1 = ((NumberTerm)args[1]).solve();
            return Math.min(n0,n1);
        } else if (args[0].isList()) {
            double min = Double.MAX_VALUE;
            for (Term t: (ListTerm)args[0]) {
                if (t.isNumeric()) {
                    double n = ((NumberTerm)t).solve();
                    if (n < min)
                        min = n;
                }
            }
            return min;
        }
        throw new JasonException(getName()+" is not implemented for type '"+args[0]+"'.");
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1 || a == 2;
    }
}
