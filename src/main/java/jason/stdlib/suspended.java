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
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

import java.util.Map;

/**
  <p>Internal action: <b><code>.suspended(<i>G</i>, <i>R</i>)</code></b>.
  
  <p>Description: checks whether goal <i>G</i> belongs to a suspended intention. <i>R</i> (a String) 
  unifies with the reason for the
  suspend (waiting action to be performed, .wait, ....). 
  
  The literal <i>G</i>
  represents a suspended goal if there is a triggering event <code>+!G</code> in any plan within
  any intention in PI or PA.
  
  <p>Example:<ul> 

  <li> <code>.suspended(go(1,3),R)</code>: true if <code>go(1,3)</code>
  is a suspended goal. <code>R</code> unifies with "act" if the reason for being suspended
  is an action waiting feedback from environment. 

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.suspend
  @see jason.stdlib.resume

*/
public class suspended extends DefaultInternalAction {

    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }

    private static final Term aAct = new StringTermImpl("act");

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Circumstance C = ts.getC();
        Trigger teGoal = new Trigger(TEOperator.add, TEType.achieve, (Literal)args[0]);

        // search in PA
        for (ActionExec a: C.getPendingActions().values())
            if (a.getIntention().hasTrigger(teGoal, un))
                return un.unifies(args[1], aAct);

        // search in PI
        Map<String, Intention> pi = C.getPendingIntentions();
        for (String reason: pi.keySet())
            if (pi.get(reason).hasTrigger(teGoal, un))
                return un.unifies(args[1], new StringTermImpl(reason));

        return false;
    }
}
