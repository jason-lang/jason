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
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
  <p>Internal action: <b><code>.save_agent</code></b>.
  
  <p>Description: stores the beliefs, rules, and plans of the agent into a file.
  
  <p>Parameters:<ul>
  
  <li>+ file name (atom, string, or variable): the name of the file. 

  <li><i>+ initial goals</i> (list -- optional): list of initial goals that will be included in the file.

  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.save_agent("/tmp/x.asl")</code>: save the agent at file "/tmp/x.asl".</li>

  <li> <code>.save_agent("/tmp/x.asl", [start, say(hello)])</code>: includes <code>start</code> and <code>say(hello)</code> as initial goals.</li>
  </ul>

  @see jason.stdlib.kill_agent
  @see jason.stdlib.create_agent
  @see jason.stdlib.stopMAS
  @see jason.runtime.RuntimeServicesInfraTier
*/
public class save_agent extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 2; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        
        String fileName = null;
        if (args[0].isString())
            fileName = ((StringTerm)args[0]).getString();
        else
            fileName = args[0].toString();
        
        ListTerm goals = new ListTermImpl();
        if (args.length > 1)
            goals = (ListTerm)args[1];

        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        
        // store beliefs (and rules)
        out.append("// beliefs and rules\n");
        for (Literal b: ts.getAg().getBB()) {
            b = b.copy();
            b.delSource(BeliefBase.ASelf);
            out.append(b+".\n");
        }
        
        // store initial goals
        out.append("\n\n// initial goals\n");
        for (Term g: goals) {
            out.append("!"+g+".\n");
        }

        
        // store plans
        out.append("\n\n// plans\n");
        for (Plan p: ts.getAg().getPL()) {
            if (!p.getLabel().toString().startsWith("kqmlReceived") && !p.getLabel().toString().startsWith("kqmlError"))
                out.append(p+"\n");
        }
        out.close();
        return true;
    }
    
}
