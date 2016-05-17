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


package jason.control;

import jason.runtime.RuntimeServicesInfraTier;

import org.w3c.dom.Document;


/** 
 *  This interface is implemented in the infrastructure tier (distributed/centralised)
 *  to provide methods that the <b>user</b> controller may call.
 */
public interface ExecutionControlInfraTier {

    /**
     * Informs an agent to continue to its next reasoning cycle.
     */
    public void informAgToPerformCycle(String agName, int cycle);

    /**
     * Informs all agents to continue to its next reasoning cycle.
     */
    public void informAllAgsToPerformCycle(int cycle);

    /**
     * Gets the agent state (beliefs, intentions, plans, ...)
     * as an XML document
     */
    public Document getAgState(String agName);

    /** Gets an object with infrastructure runtime services */
    public RuntimeServicesInfraTier getRuntimeServices();
}
