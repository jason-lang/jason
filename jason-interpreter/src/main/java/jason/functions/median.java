package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import java.util.Collections;
import java.util.List;

/**
<p>Function: <b><code>math.median(L)</code></b>: returns the median of values in L.

<p>Examples:<ul>
<li> <code>math.median([1,3])</code>: returns 2.</li>
<li> <code>math.median([1,1,3])</code>: returns 1.</li>
<li> <code>math.median([1,1,3,a])</code>: fail.</li>
<li> <code>math.median([])</code>: fail.</li>
</ul>

@author Jomi

@see jason.functions.Min
@see jason.functions.Max
@see jason.functions.Sum
@see jason.functions.mean

*/
public class median extends DefaultArithFunction  {

    public String getName() {
        return "math.median";
    }

    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        if (args[0].isList()) {
            List<Term> l = ((ListTerm) args[0]).getAsList();

            for (Term t: l)
                if (!t.isNumeric())
                    throw new JasonException(getName()+" needs '"+args[0]+"' made of numeric elements.");

            if (l.size() == 0)
                throw new JasonException(getName()+" could not define a median for the empty set '"+args[0]+"'.");

            Collections.sort(l);
            if (l.size() % 2 == 0) { // if it is a even number
                return (((NumberTerm) l.get(l.size() / 2 - 1)).solve() + ((NumberTerm) l.get(l.size() / 2)).solve()) / 2;
            } else {
                return ((NumberTerm) l.get((int) l.size() / 2)).solve();
            }
        }
        throw new JasonException(getName()+" is not implemented for type '"+args[0]+"'.");
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
}
