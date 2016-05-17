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
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;

/**
  <p>Internal action: <b><code>.include</code></b>.
  
  <p>Description: loads an .asl file, i.e., includes beliefs, goals, and plans from a file.
  
  <p>Parameters:<ul>
  <li>+ the file (string): the file name.<br/>
  </ul>
  
  <p>Examples:<ul>
  <li> <code>.include("x.asl")</code>.</li>
  </ul>

 */
public class include extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 2; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isString())
            throw JasonException.createWrongArgument(this,"first argument must be a string.");
        if (args.length > 1 && !args[1].isAtom())
            throw JasonException.createWrongArgument(this,"second argument (the namespace) must be an atom.");
    }
    
    Atom ns = Literal.DefaultNS;
    
    @Override
    public Term[] prepareArguments(Literal body, Unifier un) {
        ns = body.getNS();
        return super.prepareArguments(body, un);
    }
    
    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        
        Agent ag = ts.getAg();
        Pred inc = new Pred(ns, "include");
        inc.addTerms(args);
        
        Agent result = ((Include)DirectiveProcessor.getDirective("include")).process(
                inc, 
                ag, 
                null);
        
        ag.importComponents(result);
        ag.addInitialBelsInBB();
        ag.addInitialGoalsInTS();
        return true;
    }    
}
