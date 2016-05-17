package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
<p>Function: <b><code>math.sum(L)</code></b>: sums all values of L.

<p>Examples:<ul>
<li> <code>math.sum([1,3])</code>: returns 4.</li>
<li> <code>math.sum([3,a,"s",5])</code>: returns 8.</li>
<li> <code>math.sum([])</code>: returns 0.</li>
</ul>
 
@author Jomi 

@see jason.functions.Min 
@see jason.functions.Max
@see jason.functions.Average

*/
public class Sum extends DefaultArithFunction  {

    public String getName() {
        return "math.sum";
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isList()) {
            double sum = 0;
            for (Term t: (ListTerm)args[0])
                if (t.isNumeric())
                    sum += ((NumberTerm)t).solve();
            return sum;
        }
        throw new JasonException(getName()+" is not implemented for type '"+args[0]+"'.");
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
}
