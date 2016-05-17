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


package jason.environment;

import jason.asSyntax.Structure;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;

/** 
 * This interface is implemented by the infrastructure tier (Saci/Centralised/...) to provide concrete implementation of the environment.
 */
public interface EnvironmentInfraTier {

    /** 
     * Sends a message to the given agents notifying them that the environment has changed 
     * (called by the user environment). If no agent is informed, the notification is sent 
     * to all agents.
     */
    public void informAgsEnvironmentChanged(String... agents);

    /**
     * Sends a message to a set of agents notifying them that the environment has changed. 
     * The collection has the agents' names. 
     * (called by the user environment).
     * 
     * @deprecated use the informAgsEnvironmentChanged with String... parameter 
     */
    public void informAgsEnvironmentChanged(Collection<String> agents);

    /** Gets an object with infrastructure runtime services */
    public RuntimeServicesInfraTier getRuntimeServices();
    
    /** returns true if the infrastructure environment is running */
    public boolean isRunning();

    /** called by the user implementation of the environment when the action was executed */
    public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData);
    
}
