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
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.perceive</code></b>.
  
  <p>Description: forces the agent architecture to do perception of the
  environment immediately. It is normally used when the number of reasoning
  cycles before perception takes place was changed (this is normally at every
  cycle).

  <p>Example:<ul> 

  <li> <code>.perceive</code>.</li>

  </ul>
*/
public class perceive extends DefaultInternalAction {
    @Override public int getMinArgs() { return 0; }
    @Override public int getMaxArgs() { return 0; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ts.getAg().buf(ts.getUserAgArch().perceive());
        return true;
    }
}
