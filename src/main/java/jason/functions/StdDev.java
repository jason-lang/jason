package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
<p>Function: <b><code>math.std_dev(L)</code></b>: returns the standard deviation of all values of L.

<p>Examples:<ul>
<li> <code>math.std_dev([1,2,3])</code>: returns 1.</li>
<li> <code>math.std_dev([])</code>: returns 0.</li>
</ul>
 
@author Francisco Grimaldo 

@see jason.functions.Min 
@see jason.functions.Max
@see jason.functions.Sum
@see jason.functions.Average

*/
public class StdDev extends DefaultArithFunction  {

    public String getName() {
        return "math.std_dev";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isList()) {
            double sum = 0, squareSum = 0, num;
            int    n = 0;
            for (Term t: (ListTerm)args[0])
                if (t.isNumeric()) {
                    if (t.isNumeric()) {
                        num = ((NumberTerm)t).solve();
                        sum += num;
                        n++;
                    }
                }
            double mean = sum  / n;
            for (Term t: (ListTerm)args[0])
                if (t.isNumeric()) {
                    num = ((NumberTerm)t).solve();
                    squareSum += (num - mean) * (num - mean);
                }
            return Math.sqrt( squareSum/(n - 1) );
        }
        throw new JasonException(getName()+" is not implemented for type '"+args[0]+"'.");
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
}
