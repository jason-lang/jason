import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;
import jason.infra.jade.ContainerAllocation;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class allocates the same number of agents to each container
 * 
 * @author Jomi
 *
 */
public class myAllocator implements ContainerAllocation {

    List<String> containers = new ArrayList<String>();
    Map<String,String> allocation = new HashMap<String,String>();
    
    public void init(String[] args, MAS2JProject project)  {
        // args[0] is the list of containners
        try {
            //containers.add("Main-Container");
            for (Term t: ASSyntax.parseList(args[0])) {
                containers.add( t.toString() );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // computes the number of agents in the project
        int nbAgs = 0;
        for (AgentParameters ap : project.getAgents()) {
            nbAgs += ap.getNbInstances();
        }
        
        int agsByContainer = nbAgs / containers.size();
        System.out.println(agsByContainer+" agents will run in each container.");
        
        // create allocation
        int i=0;
        for (AgentParameters ap : project.getAgents()) {
            String agName = ap.name;
            for (int cAg = 0; cAg < ap.getNbInstances(); cAg++) {
                String numberedAg = agName;
                if (ap.getNbInstances() > 1) {
                    numberedAg += (cAg + 1);
                }
                String c = containers.get( i % containers.size());
                System.out.println("  - agent "+numberedAg+" will run at "+c);
                allocation.put(numberedAg, c);
                i++;
            }
        }
    }
    
    public List<String> getContainers() {
        return containers;
    }
    
    public String allocateAgent(String agentName) {
        return allocation.get(agentName);
    }

}
