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
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.util.Iterator;

public class namespace extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isAtom() & !args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument must be an atom or variable.");
    }
    
    @Override public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);

        if (!args[0].isVar()) {
            return ts.getAg().getBB().getNameSpaces().contains(args[0]); 
        } else {
            return new Iterator<Unifier>() {
                Iterator<Atom> i  = ts.getAg().getBB().getNameSpaces().iterator();
                Unifier        n  = null;
                
                {
                    next(); // consume the first (and set first n value, i.e. the first solution)
                }
                
                public boolean hasNext() {
                    return n != null;
                }

                public Unifier next() {
                    Unifier c = n;
                    
                    n = un.clone();
                    if (i.hasNext()) { 
                        if (!n.unifiesNoUndo(args[0], i.next()))
                            next();
                    } else {
                        n = null;
                    }
                    
                    return c;
                }
                
                public void remove() {}
            };            
        }
    }
    
}
