package jason.infra.jade;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JadeRuntimeServices implements RuntimeServicesInfraTier {

    private static Logger logger  = Logger.getLogger(JadeRuntimeServices.class.getName());
    
    private ContainerController cc;
    
    private Agent jadeAgent;
    
    JadeRuntimeServices(ContainerController cc, Agent ag) {
        this.cc = cc;
        jadeAgent = ag;
    }
    
    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts) throws Exception {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Creating jade agent " + agName + "from source " + agSource + "(agClass=" + agClass + ", archClass=" + archClasses + ", settings=" + stts);
            }
    
            AgentParameters ap = new AgentParameters();
            ap.setAgClass(agClass);
            ap.addArchClass(archClasses);
            ap.setBB(bbPars);
            ap.asSource = new File(agSource);
            
            if (stts == null) stts = new Settings();
            
            cc.createNewAgent(agName, JadeAgArch.class.getName(), new Object[] { ap, false, false }).start();
            
            return agName;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating agent " + agName, e);
        }
        return null;
    }
    
    public void startAgent(String agName) {
        // nothing to do, the jade create new agent is enough
    }

    public AgArch clone(jason.asSemantics.Agent source, List<String> archClasses, String agName) throws JasonException {
        throw new JasonException("clone for JADE is not implemented!");
    }

    public Set<String> getAgentsNames() {
        // TODO: make a cache list and update it when a new agent enters the system
        if (jadeAgent == null) return null;
        try {
            Set<String> ags = new HashSet<String>();
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("jason");
            sd.setName(JadeAgArch.dfName);
            template.addServices(sd);
            DFAgentDescription[] ans = DFService.search(jadeAgent, template);
            for (int i=0; i<ans.length; i++) {
                ags.add(ans[i].getName().getLocalName());                
            }
            /*
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults( new Long(-1) );
            AMSAgentDescription[] all = AMSService.search( jadeAgent, new AMSAgentDescription(), c);
            for (AMSAgentDescription ad: all) {
                AID agentID = ad.getName();
                if (    !agentID.getName().startsWith("ams@") && 
                        !agentID.getName().startsWith("df@") &&
                        !agentID.getName().startsWith(RunJadeMAS.environmentName) &&
                        !agentID.getName().startsWith(RunJadeMAS.controllerName)
                   ) {
                    ags.add(agentID.getLocalName());                
                }
            } 
            */       
            return ags;
            //logger.warning("getAgentsName is not implemented yet!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents' name", e);
        }        
        return null;
    }

    public int getAgentsQty() {
        try {
            return getAgentsNames().size();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents qty", e);
            return 0;
        }
    }

    public boolean killAgent(String agName, String byAg) {
        try {
            AgentController ac = cc.getAgent(agName);
            if (ac == null) {
                logger.warning("Agent "+agName+" does not exist!");
            } else {
                // TODO: if (ag.getTS().getAg().canBeKilledBy(byAg)) 
                ac.kill();    
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error killing agent", e);
        }
        return false;
    }

   
    public void stopMAS() throws Exception {
        if (cc != null) {
            new Thread() { // this command should not block the agent!
                public void run() {  
                    try {
                        cc.getPlatformController().kill();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }  
                }
            }.start();
        }
    }

}
