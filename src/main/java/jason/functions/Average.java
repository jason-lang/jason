package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
<p>Function: <b><code>math.average(L)</code></b>: returns the average of all values of L.

<p>Examples:<ul>
<li> <code>math.average([1,3])</code>: returns 2.</li>
<li> <code>math.average([])</code>: fail.</li>
</ul>
 
@author Jomi 

@see jason.functions.Min 
@see jason.functions.Max
@see jason.functions.Sum

*/
public class Average extends DefaultArithFunction  {

    public String getName() {
        return "math.average";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isList()) {
            double sum = 0;
            int    n = 0;
            for (Term t: (ListTerm)args[0])
                if (t.isNumeric()) {
                    sum += ((NumberTerm)t).solve();
                    n++;
                }
            return sum  / n;
        }
        throw new JasonException(getName()+" is not implemented for type '"+args[0]+"'.");
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
}
