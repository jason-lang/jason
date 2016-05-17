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
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;

import java.util.List;

/**
  <p>Internal action: <b><code>.relevant_plans</code></b>.
  
  <p>Description: gets all relevant plans for some triggering event. This
  internal action is used, for example, to answer "askHow" messages.

  <p>Parameters:<ul>
  
  <li>+ trigger (trigger): the triggering event, enclosed by { and }.</li>
  
  <li>- plans (list of plan terms): the list of plan terms corresponding to 
  the code of the relevant plans.</li>
  
  <li><i>- labels</i> (list, optional): the list of labels of the plans.</li>

  </ul>
  
  <p>Example:<ul> 

  <li> <code>.relevant_plans({+!go(X,Y)},LP)</code>: unifies LP with a list of
  all plans in the agent's plan library that are relevant for the triggering
  event <code>+!go(X,Y)</code>.</li>

  <li> <code>.relevant_plans({+!go(X,Y)},LP, LL)</code>: same as above but also 
  unifies LL with a list of labels of plans in LP.</li>

  <li> <code>.relevant_plans({+!_},_,LL)</code>: gets the labels of all achievement goals.</li>

  </ul>

  @see jason.stdlib.add_plan
  @see jason.stdlib.plan_label
  @see jason.stdlib.remove_plan


 */
public class relevant_plans extends DefaultInternalAction {

    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 3; }
    
    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        
		Trigger te = null;
		try {
		    te = Trigger.tryToGetTrigger(args[0]);
        } catch (ParseException e) {}
        if (te == null)
            throw JasonException.createWrongArgument(this,"first argument '"+args[0]+"' must follow the syntax of a trigger.");
        
		ListTerm labels = new ListTermImpl();
		ListTerm lt = new ListTermImpl();
		ListTerm last = lt;
        List<Option> rp = ts.relevantPlans(te);
        if (rp != null) {
            for (Option opt: rp) {
                // remove sources (this IA is used for communication)
                Plan np = (Plan)opt.getPlan().clone();
                if (np.getLabel() != null) 
                    np.getLabel().delSources();
                np.setAsPlanTerm(true);
                np.makeVarsAnnon();
                last = last.append(np);
                if (args.length == 3) 
                    labels.add(np.getLabel());
            }
		}

        boolean ok = un.unifies(lt, args[1]); // args[1] is a var;
        if (ok && args.length == 3)
            ok = un.unifies(labels, args[2]);
        
        return ok;
	}
}
