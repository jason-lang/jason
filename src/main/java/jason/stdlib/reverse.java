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
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.reverse</code></b>.

  <p>Description: reverses strings or lists. 

  <p>Parameters:<ul>
  <li>+ arg[0] (list or string): the list to be reversed.<br/>
  <li>+/- arg[1]: the result. 
  </ul>

  <p>Examples:<ul>
  <li> <code>.reverse("abc",X)</code>: <code>X</code> unifies with "cba".
  <li> <code>.reverse("[a,b,c]",X)</code>: <code>X</code> unifies with "[c,b,a]".
  <li> <code>.reverse("[a,b,c|T]",X)</code>: <code>X</code> unifies with "[c,b,a|T]".
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.member
  @see jason.stdlib.min
  @see jason.stdlib.sort
  @see jason.stdlib.substring

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
public class reverse extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new reverse();
        return singleton;
    }
    
    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        
        if (args[0].isList()) {
            // list reverse
            if (!args[1].isVar() && !args[1].isList())
                throw JasonException.createWrongArgument(this,"last argument '"+args[1]+"' must be a list or a variable.");
            
            return un.unifies(((ListTerm)args[0]).reverse(), args[1]);
        
        } else {
            // string reverse
            if (!args[1].isVar() && !args[1].isString())
                throw JasonException.createWrongArgument(this,"last argument '"+args[1]+"' must be a string or a variable.");
            String vl = args[0].toString();
            if (args[0].isString())
                vl = ((StringTerm)args[0]).getString();

            return un.unifies(new StringTermImpl(new StringBuilder(vl).reverse().toString()), args[1]);
        }
    }
}
