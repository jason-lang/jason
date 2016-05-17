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
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesInfraTier;

/**
  <p>Internal action: <b><code>.all_names</code></b>.
  
  <p>Description: get the names of all agents in the system.
  This identification is given by the runtime
  infrastructure of the system (centralised, saci, jade, ...)
  
  <p>Parameters:<ul>  
  <li>+/- names (list).<br/>
  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.all_names(L)</code>: unifies with L a list of all agents in the system.</li>

  </ul>

  @see jason.stdlib.my_name
  @see jason.runtime.RuntimeServicesInfraTier
*/
public class all_names extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        RuntimeServicesInfraTier rs = ts.getUserAgArch().getRuntimeServices();
        ListTerm ln = new ListTermImpl();
        ListTerm tail = ln;
        for (String a: rs.getAgentsNames()) {
            tail = tail.append(new Atom(a));
        }
        return un.unifies(args[0], ln);
    }
}
