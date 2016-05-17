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
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.my_name</code></b>.
  
  <p>Description: gets the agent's unique identification in the
  multi-agent system. This identification is given by the runtime
  infrastructure of the system (centralised, saci, jade, ...).
  
  <p>Parameter:<ul>
  
  <li>+/- name (atom): if this is a variable, unifies the agent
  name and the variable; if it is an atom, succeeds if the atom is equal to
  the agent's name.<br/>

  </ul>
  
  <p>Example:<ul> 

  <li> <code>.my_name(N)</code>: unifies <code>N</code> with the
  agent's name.</li>

  </ul>

  @see jason.stdlib.send
  @see jason.stdlib.broadcast

  @see jason.stdlib.all_names

 */
public class my_name extends DefaultInternalAction {
	
	private static InternalAction singleton = null;
	public static InternalAction create() {
		if (singleton == null) 
			singleton = new my_name();
		return singleton;
	}

	@Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return un.unifies(args[0], new Atom(ts.getUserAgArch().getAgName()));
	}
}
