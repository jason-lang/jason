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

/**

  <p>Internal action: <b><code>.findall(Term,Query,List)</code></b>.

  <p>Description: builds a <i>List</i> of all instantiations of
  <i>Term</i> which make <i>Query</i> a logical consequence of the
  agent's BB.

  <p>Parameters:<ul>

  <li>+ term (variable or structure): the variable or structure whose
  instances will "populate" the list.<br/>

  <li>+ query (logical formula): the formula used to find literals in the belief base;
  is has the same syntax as the plan context.
  <br/>

  <li>+/- result (list): the result list populated with found solutions for the query.<br/>

  </ul>

  <p>Examples assuming the BB is currently
  {a(30),a(20),b(1,2),b(3,4),b(5,6),c(100),c(200),c(100)}:

  <ul>

  <li> <code>.findall(X,a(X),L)</code>: <code>L</code> unifies with
  <code>[30,20]</code>.</li>

  <li> <code>.findall(c(Y,X),b(X,Y),L)</code>: <code>L</code> unifies
  with <code>[c(2,1),c(4,3),c(6,5)]</code>.</li>

  <li> <code>.findall(r(X,V1,V2), (a(X) & b(V1,V2) & V1*V2 < X), L)</code>: <code>L</code> unifies
  with <code>[r(30,1,2),r(30,3,4),r(20,1,2),r(20,3,4)]</code>.</li>
  </ul>

  <li> <code>.findall(X,c(X),L)</code>: <code>L</code> unifies with
  <code>[100,200,100]</code>.</li>


  @see jason.stdlib.count
  @see jason.stdlib.setof
*/
@Manual(
		literal=".findall(term,query,result)",
		hint="builds a List of all instantiations of referred term which make query a logical consequence of the agent's BB",
		argsHint= {
				"the variable or structure whose instances will \"populate\" the list",
				"the formula used to find literals in the belief base",
				"the result list populated with found solutions for the query"
		},
		argsType= {
				"variable or structure",
				"logical formula",
				"list"
		},
		examples= {
				".findall(X,a(X),L): assuming current BB with {a(30),a(20),b(1,2),b(3,4),b(5,6)}, L unifies with [30,20]",
				".findall(c(Y,X),b(X,Y),L): assuming current BB with {a(30),a(20),b(1,2),b(3,4),b(5,6)}, L unifies with [c(2,1),c(4,3),c(6,5)]",
				".findall(r(X,V1,V2), (a(X) & b(V1,V2) & V1*V2 < X), L): assuming current BB with {a(30),a(20),b(1,2),b(3,4),b(5,6)}, L unifies with [r(30,1,2),r(30,3,4),r(20,1,2),r(20,3,4)]",
				".findall(X, c(X), L): assuming current BB with {c(100),c(200),c(100)}, L unifies with [100,200,100]"
		},
		seeAlso= {
				"jason.stdlib.count",
				"jason.stdlib.setof"
		}
	)
@SuppressWarnings("serial")
public class findall extends DefaultInternalAction {

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
        ListTerm all = new ListTermImpl();
        ListTerm tail = all;
        Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un);
        while (iu.hasNext()) {
            tail = tail.append(var.capply(iu.next()));
        }
        return un.unifies(args[2], all);
    }
}
