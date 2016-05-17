// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.util.logging.Level;


/**
  <p>Internal action: <b><code>.println</code></b>.
  
  <p>Description: used for printing messages to the console. Exactly as for
  <code>.print</code> except that a new line is printed after the parameters.


  @see jason.stdlib.print

*/
public class println extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new println();
        return singleton;
    }
    
    protected String getNewLine() {
        return "\n";
    }
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String sout = argsToString(args);
        
        if (ts != null && ts.getSettings().logLevel() != Level.WARNING) {
            ts.getLogger().info(sout.toString());
        } else {
            System.out.print(sout.toString() + getNewLine());
        }
        
        return true;
    }

    protected String argsToString(Term[] args) {
        StringBuilder sout = new StringBuilder();
        //try {
        //    if (ts.getSettings().logLevel() != Level.WARNING && args.length > 0) {
        //        sout = new StringBuilder();
        //    }
        //} catch (Exception e) {}
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].isString()) {
                StringTerm st = (StringTerm)args[i];
                sout.append(st.getString());
            } else {
                Term t = args[i];
                if (! t.isVar()) {
                    sout.append(t);
                } else {
                    sout.append(t+"<no-value>");
                }
            }
        }
        return sout.toString();
    }
}
