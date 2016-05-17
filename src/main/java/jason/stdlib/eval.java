//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;

import java.util.Iterator;

/**

  <p>Internal action: <b><code>.eval(Var,Logical Expression)</code></b>.
  
  <p>Description: evaluates the logical expression (which computes to true or false), the result is unified with 
  <i>Var</i>.

  <p>Parameters:<ul>
  
  <li>+ term (variable, atoms true or false): the variable that unifies with the result of evaluation.<br/>
  
  <li>+ query (logical formula): the formula that is evaluated.
  <br/>

  </ul>
  
  <p>Examples:

  <ul>

  <li> <code>.eval(X, true | false)</code>: <code>X</code> unifies with
  <code>true</code>.</li>

  <li> <code>.eval(X, 3<5 & not 4+2<3)</code>: <code>X</code> unifies
  with <code>true</code>.</li>
*/
public class eval extends DefaultInternalAction {

    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }

    @Override public Term[] prepareArguments(Literal body, Unifier un) {
        return body.getTermsArray(); // we do not need clone neither apply for this internal action
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (! (args[1] instanceof LogicalFormula))
            throw JasonException.createWrongArgument(this,"second argument must be a logical formula");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        LogicalFormula logExpr = (LogicalFormula)args[1];
        Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un);
        if (iu.hasNext()) {
            return un.unifies(args[0], Literal.LTrue);
        } else {
            return un.unifies(args[0], Literal.LFalse);
        }
    }
}
