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
import jason.asSyntax.Term;

import java.util.List;

/**
  <p>Internal action: <b><code>.asserta</code></b>.
  
  <p>Description: adds a new belief as the "+" (or "+<") operator. However, it can be used in prolog like rules.
  
  <p>Parameters:<ul>
  <li>+ belief (literal): the belief that will be added in the being of the belief base.<br/>
  </ul>
  
  <p>Examples:<ul>
  <li> <code>.asserta(p)</code>: adds <code>p</code> in the belief base.</li>
  </ul>
  
  @see jason.stdlib.assertz
  @see jason.stdlib.abolish

 */
public class asserta extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral())
            if (!args[0].isGround() && !args[0].isRule())
                throw JasonException.createWrongArgument(this,"first argument must be a ground literal (or rule).");
    }
    
    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        List<Literal>[] result = ts.getAg().brf((Literal)args[0],null,null,false); 
        if (result != null) { // really added something
            // generate events
            ts.updateEvents(result,null);
        }
        return true;
    }
    
}
