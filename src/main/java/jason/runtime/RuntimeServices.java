package jason.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.mas2j.ClassParameters;

/**
 * This interface is implemented by the infrastructure
 * (Jade/Centralised/...) to provide concrete runtime services.
 */
public interface RuntimeServices {

    /**
     * Creates a new agent with <i>agName</i> from source
     * <i>agSource</i>, using <i>agClass</i> as agent class (default
     * value is "jason.asSemantics.Agent"), <i>archClass</i> as agent
     * architecture class (default value is "jason.architecture.AgArch"),
     * <i>bbPars</i> as the belief base
     * class (default value is "DefaultBeliefBase"), <i>stts</i> as
     * Settings (default value is new Settings()), and
     * <i>father</i> is the agent creating this agent (null is none).
     *
     * <p> Example: createAgent("bob", "bob.asl", "mypkg.MyAgent", null, null, null);
     *
     * Returns the name of the agent
     */
    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception;

    /** starts an agent (e.g. create thread for it) */
    public void startAgent(String agName);

    /**
     * Clones an agent
     *
     * @param source: the agent used as source for beliefs, plans, ...
     * @param archClassName: agent architectures that will be used
     * @param agName: the name of the clone
     * @return the agent arch created
     * @throws JasonException
     */
    public AgArch clone(Agent source, List<String> archClasses, String agName) throws JasonException;

    /**
     * Kills the agent named <i>agName</i> as a requested by <i>byAg</i>.
     * Agent.stopAg() method is called before the agent is removed.
     */
    public boolean killAgent(String agName, String byAg);

    /** Returns a set of all agents' name */
    public Set<String> getAgentsNames();

    /** Gets the number of agents in the MAS. */
    public int getAgentsQty();

    /** Stops all MAS (the agents, the environment, the controller, ...) */
    public void stopMAS() throws Exception;
    
    default public void dfRegister(String agName, Literal service) {}
    default public void dfDeRegister(String agName, Literal service) {}
    default public Collection<String> dfSearch(Literal service) { return new ArrayList<>(); }
    default public void dfSubscribe(String agName, Literal service) {}
}
