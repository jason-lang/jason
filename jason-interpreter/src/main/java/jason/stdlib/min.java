package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;

import java.util.Iterator;

/**
<p>Internal action: <b><code>.min</code></b>.

    <p>Description: gets the minimum value of a list of terms, using
    the "natural" order of terms. Between
different types of terms, the following order is
used:<br>

numbers &lt; atoms &lt; structures &lt; lists

<p>Parameters:<ul>
<li>+   list (list): the list where to find the minimal term.<br/>
<li>+/- minimal (term).
</ul>

 <p>Query version:<ul>
 <li>+ term (variable or structure): the variable or structure whose
 instances will be used as min candidate.<br/>

 <li>+ query (logical formula): the formula used to find values for the term
 <br/>

 <li>+/- result (term): the min value for the query.<br/>
 </ul>


 <p>Examples:<ul>

<li> <code>.min([c,a,b],X)</code>: <code>X</code> unifies with
<code>a</code>.

<li>
<code>.min([b,c,10,g,f(10),[3,4],5,[3,10],f(4)],X)</code>:
<code>X</code> unifies with <code>5</code>.

<li>
<code>.min([3,2,5],2)</code>: true.

<li>
<code>.min([3,2,5],5)</code>: false.

<li><code>.min([],X)</code>: false.

 <li><code>.min(V, b(V), Min)</code>: unifies Min with the min V value for query b(V).


</ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.nth
  @see jason.stdlib.sort
  @see jason.stdlib.max
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
public class min extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new min();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isList() && args.length == 2)
            throw JasonException.createWrongArgument(this,"first argument must be a list when two arguments are used");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        if (args[0].isList()) {
            ListTerm list = (ListTerm) args[0];
            if (list.isEmpty()) {
                return false;
            }

            Iterator<Term> i = list.iterator();
            Term min = i.next();
            while (i.hasNext()) {
                Term t = i.next();
                if (compare(min, t)) {
                    min = t;
                }
            }
            return un.unifies(args[1], min.clone());
        }

        if (args.length == 3) { // case of .min(V, query, Min)
            Term var = args[0];
            LogicalFormula logExpr = (LogicalFormula)args[1];
            Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un);
            Term result = null;
            while (iu.hasNext()) {
                var value = var.capply(iu.next());
                if (result == null || compare(result,value))
                    result = value;
            }
            return un.unifies(args[2], result);
        }
        return false;
    }

    protected boolean compare(Term a, Term t) {
        return a.compareTo(t) > 0;
    }
}
