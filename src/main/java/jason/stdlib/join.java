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
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.Term;
import jason.stdlib.fork.ForkData;

/** injected by .fork */
public class join extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new join();
        return singleton;
    }
    
    @Override public Term[] prepareArguments(Literal body, Unifier un) {
        return body.getTermsArray();
    }
    
    @Override protected void checkArguments(Term[] args) throws JasonException {
    }

    @Override public boolean suspendIntention()   { return true;  }    
    @Override public boolean canBeUsedInContext() { return false; }
        
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        
        Intention currentInt = ts.getC().getSelectedIntention();
        ForkData fd = (ForkData) ((ObjectTerm)args[0]).getObject();
        fd.toFinish--;
        //System.out.println("** in join for "+currentInt.getId()+ " with "+fd);
        
        // in the case of fork and, all intentions should be finished to continue
        if (fd.isAnd) {
            if (fd.toFinish == 0) {
                //System.out.println("join finished!");
                currentInt.peek().removeCurrentStep();
                ts.getC().addIntention(currentInt);
            }
        } else {
            // the first intention has finished, drop others
            fd.intentions.remove(currentInt);
            for (Intention i: fd.intentions) {
                //System.out.println("drop "+i.getId());
                drop_intention.dropInt(ts.getC(), i);
            }
            currentInt.peek().removeCurrentStep();
            ts.getC().addIntention(currentInt);
        }

        return true;
    }
    
}
