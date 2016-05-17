package jason.infra.jade;

import jason.mas2j.MAS2JProject;

import java.util.List;

/** 
 * This interface has to be implemented by classes that
 * customises the agent allocation to containers/hosts in 
 * the JADE infrastructure
 *  
 * @author Jomi
 */
public interface ContainerAllocation {
    public void init(String[] args, MAS2JProject project);
    public List<String> getContainers();
    public String allocateAgent(String agentName);
}
