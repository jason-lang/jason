package jason.stdlib;

import jason.asSemantics.InternalAction;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.max</code></b>.

<p>Description: gets the maximum value within a list of terms, 
using the "natural" order for each type of term. Between 
different types of terms, the following order is
used:<br>

numbers &lt; atoms &lt; structures &lt; lists 

<p>Parameters:<ul>
<li>+   list (list): the list where to find the maximum term.<br/>
<li>+/- maximum (term). 
</ul>

<p>Examples:<ul>

<li> <code>.max([c,a,b],X)</code>: <code>X</code> unifies with
<code>c</code>.

<li>
<code>.max([b,c,10,g,f(10),5,f(4)],X)</code>:
<code>X</code> unifies with <code>f(10)</code>.

<li>
<code>.max([3,2,5],2])</code>: false.

<li>
<code>.max([3,2,5],5)</code>: true.

<li>
<code>.max([],X)</code>: false.

</ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.nth
  @see jason.stdlib.sort
  @see jason.stdlib.min
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
public class max extends min {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new max();
        return singleton;
    }

    @Override
    protected boolean compare(Term a, Term t) {
        return a.compareTo(t) < 0;
    }
}
