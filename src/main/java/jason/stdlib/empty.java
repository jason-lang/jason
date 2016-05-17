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

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.empty</code></b>.

  <p>Description: checks whether a list has at least one term. 

  <p>Parameters:<ul>
  <li>+ argument (string or list): the term whose length is to be determined.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.empty([])</code>: true.
  <li> <code>.empty([a,b])</code>: false.
  <li> <code>.empty("a")</code>: false.
  </ul>
  
 */
public class empty extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new empty();
        return singleton;
    }
    
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        Term l1 = args[0];
        if (l1.isList()) {
            ListTerm lt = (ListTerm) l1;
            return lt.isEmpty();
        } else if (l1.isString()) {
            StringTerm st = (StringTerm) l1;
            return st.getString().isEmpty();
        }
        return false;
    }
}
