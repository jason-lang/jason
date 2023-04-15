package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.stdlib.length;

/**
  <p>Function: <b><code>.length(L)</code></b>: returns the size of either the list or string L.

  <p>Examples:<ul>
  <li> <code>.length("aa")</code>: returns 2.</li>
  <li> <code>.length([a,b,c])</code>: returns 3.</li>
  </ul>

  @see jason.stdlib.length internal action version of length

  @author Jomi
*/
public class Length extends DefaultArithFunction  {

    public String getName() {
        return ".length";
    }

    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws JasonException {
        NumberTerm size = length.getSize(args[0]);
        if (size != null) {
            return size.solve();
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not a list or a string or a collection!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }

}
