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
  
  @see jason.functions.Count function version
*/
public class count extends DefaultInternalAction {

    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }

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
