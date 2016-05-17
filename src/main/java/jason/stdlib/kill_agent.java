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
import jason.asSyntax.StringTerm;

/**
  <p>Internal action: <b><code>.kill_agent</code></b>.
  
  <p>Description: kills the agent whose name is given as parameter. This is a
     provisional internal action to be used while more adequate mechanisms for
     creating and killing agents are being developed. In particular, note that
     an agent can kill any other agent, without any consideration on
     permissions, etc.! It is the programmers' responsibility to use this
     action.

  
  <p>Parameters:<ul>
  
  <li>+ name (atom or string): the name of the agent to be killed.<br/>

  </ul>
  
  <p>Example:<ul> 

  <li> <code>.kill_agent(bob)</code>: kills the agent named bob.</li>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.save_agent
  @see jason.stdlib.stopMAS
  @see jason.runtime.RuntimeServicesInfraTier

*/
public class kill_agent extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        String name;
        if (args[0].isString()) 
            name = ((StringTerm)args[0]).getString();
        else
            name = args[0].toString();
        return ts.getUserAgArch().getRuntimeServices().killAgent(name, ts.getUserAgArch().getAgName());
	}
}
