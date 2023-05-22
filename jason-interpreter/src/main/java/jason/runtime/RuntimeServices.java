package jason.runtime;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.mas2j.ClassParameters;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * This interface is implemented by the infrastructure
 * (Jade/Local/...) to provide concrete runtime services.
 */
public interface RuntimeServices extends Remote {

    public default String getMASName() throws RemoteException { return ""; }

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
    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception, RemoteException;

    public String getNewAgentName(String baseName) throws RemoteException;

    /** register a class to be included as new agents archs */
    default public void registerDefaultAgArch(String agArch) throws RemoteException {}

    default public Collection<String> getDefaultAgArchs() throws RemoteException { return new ArrayList<>(); }

    /** starts an agent (e.g. create thread for it) */
    public void startAgent(String agName) throws RemoteException;

    /**
     * Clones an agent and starts it
     *
     * @param source: the agent used as source for beliefs, plans, ...
     * @param archClasses: agent architectures that will be used
     * @param agName: the name of the clone
     * return the agent arch created
     * @throws JasonException
     */
    default public void clone(Agent source, List<String> archClasses, String agName) throws RemoteException, JasonException { }

    /**
     * Kills the agent named <i>agName</i> as a requested by <i>byAg</i>.
     * Agent.stopAg() method is called before the agent is removed.
     */
    public boolean killAgent(String agName, String byAg, int deadline) throws RemoteException;

    /** Returns a set of all agents' name */
    public Collection<String> getAgentsNames() throws RemoteException;

    /** Gets the number of agents in the MAS. */
    public int getAgentsQty() throws RemoteException;

    public boolean isRunning() throws RemoteException;

    default public Map<String, Object> getAgStatus(String agName) throws RemoteException { return new HashMap<>(); }

    /** gets a copy of some agent (BB, PL, ...) */
    default public Agent getAgentSnapshot(String agName) throws RemoteException { return null; }

    /** loads some ASL code into some agent, if replace is true, the previous plans from the sourceID will be removed */
    default public String loadASL(String agName, String code, String sourceId, boolean replace) throws RemoteException { return "not implemented"; }

    /** Stops all MAS (the agents, the environment, the controller, ...) */
    public void stopMAS(int deadline, boolean stopJVM, int exitValue) throws RemoteException, Exception;
    default public void stopMAS() throws Exception { stopMAS(0, true, 0); }

    default public void               dfRegister(String agName, String service, String type) throws RemoteException {}
    default public void               dfDeRegister(String agName, String service, String type) throws RemoteException {}
    default public Collection<String> dfSearch(String service, String type) throws RemoteException { return new ArrayList<>(); }
    default public void               dfSubscribe(String agName, String service, String type) throws RemoteException {}

    default public Map<String, Set<String>> getDF() throws RemoteException { return null; }
    default public Map<String,Map<String,Object>> getWP() throws RemoteException { return null; }

}
