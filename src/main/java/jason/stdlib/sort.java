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
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.Collections;
import java.util.List;

/**

  <p>Internal action: <b><code>.sort</code></b>.

  <p>Description: sorts a list of terms. The "natural" order for each type of
  terms is used. Between different types of terms, the following order is
  used:<br>

  numbers &lt; strings &lt; lists &lt; literals (by negation, arity, functor, terms, annotations) &lt; variables  

  <p>Parameters:<ul>
  <li>+   unordered list (list): the list the be sorted.<br/>
  <li>+/- ordered list (list): the sorted list. 
  </ul>

  <p>Examples:<ul>
  
  <li> <code>.sort([c,a,b],X)</code>: <code>X</code> unifies with
  <code>[a,b,c]</code>.

  <li>
  <code>.sort([C,b(4),A,4,b(1,1),"x",[],[c],[a],[b,c],[a,b],~a(3),a(e,f),b,a(3),b(3),a(10)[30],a(10)[5],a,a(d,e)],X)</code>:
  <code>X</code> unifies with
  <code>[4,"x",[],[a],[c],[a,b],[b,c],a,b,a(3),a(10)[5],a(10)[30],b(3),b(4),a(d,e),a(e,f),b(1,1),~a(3),A,C]</code>.

  <li>
  <code>.sort([3,2,5],[2,3,5])</code>: true.

  <li>
  <code>.sort([3,2,5],[a,b,c])</code>: false.

  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
public class sort extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new sort();
        return singleton;
    }
    
    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isList())
            throw JasonException.createWrongArgument(this,"first argument must be a list");
    }
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        List<Term> l = ((ListTerm) args[0]).getAsList();
        Collections.sort(l);
        return un.unifies(ASSyntax.createList(l), args[1]);
    }
}
