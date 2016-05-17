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
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;

import java.util.Collections;
import java.util.List;

/**

  <p>Internal action: <b><code>.shuffle(List,Result)</code></b>.
  
  <p>Description: shuffle the elements of the <i>List</i> and unifies the result in <i>Var</i>.

  <p>Parameters:<ul>
  
  <li>+ list: the list to be shuffled<br/>
  
  <li>- result (var): the list with the elements shuffled.<br/>
  
  </ul>

*/
public class shuffle extends DefaultInternalAction {

    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (! (args[0].isList()))
            throw JasonException.createWrongArgument(this,"first argument must be a list");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        List<Term> lt = ((ListTerm)args[0]).getAsList();
        Collections.shuffle(lt);
        ListTerm ls = new ListTermImpl();
        ls.addAll(lt);
        return un.unifies(args[1], ls);
    }
}
