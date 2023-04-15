package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Iterator;

/**
  <p>Internal action: <b><code>.count</code></b>.

  <p>Description: counts the number of occurrences of a particular belief
  (pattern) in the agent's belief base.

  <p>Parameters:<ul>

  <li>+ query (logical formula): the formula used to count literals in the belief base;
  is has the same syntax as the plan context.
  <br/>

  <li>+/- quantity (number): the number of occurrences of the belief.<br/>

  </ul>

  <p>Examples:<ul>

  <li> <code>.count(a(2,_),N)</code>: counts the number of beliefs
  that unify with <code>a(2,_)</code>; <code>N</code> unifies with
  this quantity.</li>

  <li> <code>.count((a(2,X)& X>10),N)</code>: counts the number of beliefs
  that unify with <code>a(2,X)</code> and X > 10; <code>N</code> unifies with
  this quantity.</li>

  <li> <code>.count(a(2,_),5)</code>: succeeds if the BB has exactly 5
  beliefs that unify with <code>a(2,_)</code>.</li>

  </ul>

  @see jason.stdlib.findall
  @see jason.stdlib.setof
  @see jason.stdlib.namespace

  @see jason.functions.Count function version
*/
@Manual(
		literal=".count(query,result)",
		hint="counts the number of occurrences of a belief the belief base",
		argsHint= {
				"the formula used to count literals in the belief base",
				"the resulting unification of the number of occurrences of the belief"
		},
		argsType= {
				"logical formula",
				"number"
		},
		examples= {
				".count(a(2,_),N): counts the number of beliefs that unify with a(2,_); N unifies with this quantity",
				".count((a(2,X)& X>10),N): counts the number of beliefs that unify with a(2,X) and X > 10; N unifies with this quantity",
				".count(a(2,_),5): succeeds if the BB has exactly 5 beliefs that unify with a(2,_)"
		},
		seeAlso= {
				"jason.stdlib.findall",
				"jason.functions.Count"
		}
	)
@SuppressWarnings("serial")
public class count extends DefaultInternalAction {

	@Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!(args[0] instanceof LogicalFormula))
            throw JasonException.createWrongArgument(this,"first argument must be a formula");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        LogicalFormula logExpr = (LogicalFormula)args[0];
        int n = 0;
        Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un);
        while (iu.hasNext()) {
            iu.next();
            n++;
        }
        return un.unifies(args[1], new NumberTermImpl(n));
    }
}
