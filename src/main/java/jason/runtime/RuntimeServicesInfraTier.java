package jason.runtime;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.ClassParameters;

import java.util.List;
import java.util.Set;

/**
 * This interface is implemented by the infrastructure tier
 * (Saci/Centralised/...) to provide concrete runtime services.
 */
public interface RuntimeServicesInfraTier {

    /**
     * Creates a new agent with <i>agName</i> from source
     * <i>agSource</i>, using <i>agClass</i> as agent class (default
     * value is jason.asSemantics.Agent), <i>archClass</i> as agent
     * architecture class (default value is
     * jason.architecture.AgArch), <i>bbPars</i> as the belief base
     * class (default value is DefaultBeliefBase), and <i>stts</i> as
     * Settings (default value is new Settings()).
     * 
     * <p> Example: createAgent("bob", "bob.asl", "mypkg.MyAgent", null, null, null);
     * 
     * Returns the name of the agent
     */
    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts) throws Exception;

    /** start (e.g. create thread) the agent */
    public void startAgent(String agName);
    
    /**
     * Clones an agent
     * 
     * @param source: the used as source of beliefs, plans, ...
     * @param archClassName: the architecture that will be used 
     * @param agName: the name of the clone
     * @return the agent arch created
     * @throws JasonException
     */
    public AgArch clone(Agent source, List<String> archClasses, String agName) throws JasonException;

    /**
     * Kills the agent named <i>agName</i> as a requested by <i>byAg</i>. 
     * The stopAg() method, in
     * the agent architecture is called before the agent is removed.
     */
    public boolean killAgent(String agName, String byAg);

    /** Returns a set of all agents' name */
    public Set<String> getAgentsNames();

    /** Gets the number of agents in the MAS. */
    public int getAgentsQty();

    /** Stop all MAS (the agents, the environment, the controller, ...) */
    public void stopMAS() throws Exception;
}
