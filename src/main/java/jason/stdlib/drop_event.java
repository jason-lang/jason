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
//----------------------------------------------------------------------------


package jason.stdlib;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;


/**
  <p>Internal action: <b><code>.drop_event(<i>D</i>)</code></b>.
  
  <p>Description: removes events <i>D</i> from the agent circumstance. 
  This internal action simply removes all <i>+!D</i> entries
  (those for which <code>.desire(D)</code> would succeed) <i>from the set of events only</i>;
  this action is complementary to <code>.drop_desire</code> and <code>.drop_intention</code>,
  in case a goal is to be removed only from the set of events and <i>not</i> from the set of intentions.
  No event is produced as a consequence of dropping desires from the set of events.

  <p>Example:<ul> 

  <li> <code>.drop_event(go(X,Y))</code>: removes events such as
  <code>&lt;+!go(1,3),_&gt;</code> from the set of events.

  </ul>

 
  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.intend


 */
public class drop_event extends drop_desire {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        dropEvt(ts.getC(), (Literal)args[0], un);
        return true;
    }
}
