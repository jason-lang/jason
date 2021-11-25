package jason.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.ClassParameters;

/**
 * This interface is implemented by the infrastructure
 * (Jade/Local/...) to provide concrete runtime services.
 */
public interface RuntimeServices {

    /**
     * Creates a new agent with <i>agName</i> from source
     * <i>agSource</i>, using <i>agClass</i> as agent class (default
     * value is "jason.asSemantics.Agent"), <i>archClasses</i> as agent
     * architecture classes,
     * <i>bbPars</i> as the belief base
     * class (default value is "DefaultBeliefBase"), <i>stts</i> as
     * Settings (default value is new Settings()), and
     * <i>father</i> is the agent creating this agent (null is none).
     *
     * if no archClasses is informed (null value),
     *    if fathers is informed
     *        use father's ag archs
     *    else
     *        use default ag archs (see registerDefaultAgArch)
     *
     * <p> Example: createAgent("bob", "bob.asl", "mypkg.MyAgent", null, null, null);
     *
     * Returns the name of the agent
     */
    public String createAgent(String agName, String agSource, String agClass, Collection<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception;

    public String getNewAgentName(String baseName);

    /** register a class to be included as new agents archs */
    default public void registerDefaultAgArch(String agArch) {}

    default public Collection<String> getDefaultAgArchs() { return new ArrayList<>(); }

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
    public AgArch clone(Agent source, Collection<String> archClasses, String agName) throws JasonException;

    /**
     * Kills the agent named <i>agName</i> as a requested by <i>byAg</i>.
     * Agent.stopAg() method is called before the agent is removed.
     */
    public boolean killAgent(String agName, String byAg, int deadline);

    /** Returns a set of all agents' name */
    public Collection<String> getAgentsNames();

    /** Gets the number of agents in the MAS. */
    public int getAgentsQty();

    public boolean isRunning();

    /** Stops all MAS (the agents, the environment, the controller, ...) */
    public void stopMAS(int deadline, boolean stopJVM, int exitValue) throws Exception;
    default public void stopMAS() throws Exception { stopMAS(0, true, 0); }

    default public void               dfRegister(String agName, String service, String type) {}
    default public void               dfDeRegister(String agName, String service, String type) {}
    default public Collection<String> dfSearch(String service, String type) { return new ArrayList<>(); }
    default public void               dfSubscribe(String agName, String service, String type) {}

    default public Map<String, Set<String>> getDF() { return null; }
    default public Map<String,Map<String,Object>> getWP() { return null; }

}
