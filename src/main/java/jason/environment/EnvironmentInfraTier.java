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
