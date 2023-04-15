package jason.environment;

import jason.asSyntax.Structure;
import jason.runtime.RuntimeServices;

/**
 * This interface is implemented by the infrastructure tier (Jade/Local/...) to provide concrete implementation of the environment.
 */
public interface EnvironmentInfraTier {

    /**
     * Sends a message to the given agents notifying them that the environment has changed
     * (called by the user environment). If no agent is informed, the notification is sent
     * to all agents.
     */
    public void informAgsEnvironmentChanged(String... agents);

    /** Gets an object with infrastructure runtime services */
    public RuntimeServices getRuntimeServices();

    /** returns true if the infrastructure environment is running */
    public boolean isRunning();

    /** called by the user implementation of the environment when the action was executed */
    public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData);

}
