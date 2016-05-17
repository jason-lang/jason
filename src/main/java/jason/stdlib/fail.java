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
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.fail</code></b>.
  
  <p>Description: fails the intention where it is run (an internal action that
  always returns false).

  <p>Example:<ul> 

  <li> <code>.fail</code>.</li>

  </ul>
  
 */
public class fail extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new fail();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (args.length > 0) { // add all arguments as annotations in the exception
            // find message
            Term smsg = null;
            String msg = "fail";
            for (Term t: args) {
                if (t.isStructure() && ((Structure)t).getFunctor().equals("error_msg")) {
                    smsg = t;
                    Term tm = ((Structure)t).getTerm(0);
                    if (tm.isString())
                        msg = ((StringTerm)tm).getString();
                    else
                        msg = tm.toString();
                    break;
                }
            }
            
            JasonException e = new JasonException(msg);
            for (Term t: args) {
                if (t != smsg)
                    e.addErrorAnnot(t);
            }
            throw e;
        }
        return false;
    }
}
