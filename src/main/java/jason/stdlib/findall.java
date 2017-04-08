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

  <p>Internal action: <b><code>.findall(Var,Literal,List)</code></b>.

  <p>Description: builds a <i>List</i> of all instantiations of
  <i>term</i> which make <i>query</i> a logical consequence of the
  agent's BB. Unlike in Prolog, the second argument cannot be a
  conjunction.

  <p>Parameters:<ul>

  <li>+ term (variable or structure): the variable or structure whose
  instances will "populate" the list.<br/>

  <li>+ query (logical formula): the formula used to find literals in the belief base;
  is has the same syntax as the plan context.
  <br/>

  <li>+/- result (list): the result list populated with found solutions for the query.<br/>

  </ul>

  <p>Examples assuming the BB is currently
  {a(30),a(20),b(1,2),b(3,4),b(5,6)}:

  <ul>

  <li> <code>.findall(X,a(X),L)</code>: <code>L</code> unifies with
  <code>[30,20]</code>.</li>

  <li> <code>.findall(c(Y,X),b(X,Y),L)</code>: <code>L</code> unifies
  with <code>[c(2,1),c(4,3),c(6,5)]</code>.</li>

  <li> <code>.findall(r(X,V1,V2), (a(X) & b(V1,V2) & V1*V2 < X), L)</code>: <code>L</code> unifies
  with <code>[r(30,1,2),r(30,3,4),r(20,1,2),r(20,3,4)]</code>.</li>
  </ul>


  @see jason.stdlib.count
  @see jason.stdlib.setof
*/
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
