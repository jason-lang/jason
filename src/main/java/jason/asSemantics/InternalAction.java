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

package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
 * Common interface for all internal actions.
 *  
 * @author Jomi
 */
public interface InternalAction {
    
    /** Returns true if the internal action (IA) should suspend the 
        intention where the IA is called */
    boolean suspendIntention();
    
    /** Return true if the internal action can be used in plans' context */
    boolean canBeUsedInContext();
    
    /** Prepare body's terms to be used in 'execute', normally it consist of cloning and applying each term */
    public Term[] prepareArguments(Literal body, Unifier un);
    
    /** Executes the internal action. It should return a Boolean or
     *  an Iterator<Unifier>. A true boolean return means that the IA was
     *  successfully executed. An Iterator result means that there is 
     *  more than one answer for this IA (e.g. see member internal action). */
    Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception;
    
    public void destroy() throws Exception;
}
