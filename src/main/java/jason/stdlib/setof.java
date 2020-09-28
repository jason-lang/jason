package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**

  <p>Internal action: <b><code>.setof(Term,Query,List)</code></b>.

  <p>Description: builds a <i>Set</i> of unique instantiations of
  <i>Term</i> which make <i>Query</i> a logical consequence of the
  agent's BB.

  <p>Parameters:<ul>

  <li>+ term (variable or structure): the variable or structure whose
  instances will "populate" the set.<br/>

  <li>+ query (logical formula): the formula used to find literals in the belief base;
  is has the same syntax as the plan context.
  <br/>

  <li>+/- result (list): the result set populated with found solutions for the query.<br/>

  </ul>

  <p>Examples assuming the BB is currently
  {c(100),c(200),c(100)}:

  <ul>

  <li> <code>.setof(X,c(X),L)</code>: <code>L</code> unifies with
  <code>[100,200]</code>.</li>


  @see jason.stdlib.count
  @see jason.stdlib.findall
*/
@Manual(
		literal=".setof(term,query,result)",
		hint="builds a Set of all instantiations of referred term which make query a logical consequence of the agent's BB",
		argsHint= {
				"the variable or structure whose instances will \"populate\" the set",
				"the formula used to find literals in the belief base",
				"the result set populated with found solutions for the query"
		},
		argsType= {
				"variable or structure",
				"logical formula",
				"list"
		},
		examples= {
				".setof(X,c(X),L): assuming current BB with {c(100),c(200),c(100)}, L unifies with [100,200]"
		},
		seeAlso= {
				"jason.stdlib.count",
				"jason.stdlib.findall"
		}
	)
@SuppressWarnings("serial")
public class setof extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 3;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override public Term[] prepareArguments(Literal body, Unifier un) {
        return body.getTermsArray(); // we do not need to clone nor to apply for this internal action
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (! (args[1] instanceof LogicalFormula))
            throw JasonException.createWrongArgument(this,"second argument must be a formula");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Term var = args[0];
        LogicalFormula logExpr = (LogicalFormula)args[1];
        Set<Term> all = new TreeSet<Term>();
        Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un);
        while (iu.hasNext()) {
            all.add(var.capply(iu.next()));
        }
        return un.unifies(args[2], setToList(all));
    }

    // copy the set to a new list
    private ListTerm setToList(Set<Term> set) {
        ListTerm result = new ListTermImpl();
        ListTerm tail = result;
        for (Term t: set)
            tail = tail.append(t.clone());
        return result;
    }
}
