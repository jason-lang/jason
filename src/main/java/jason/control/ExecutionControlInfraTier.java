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
