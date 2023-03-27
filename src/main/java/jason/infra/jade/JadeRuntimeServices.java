package jason.infra.jade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServices;
import jason.runtime.Settings;

public class JadeRuntimeServices implements RuntimeServices {

    private static Logger logger  = Logger.getLogger(JadeRuntimeServices.class.getName());

    private ContainerController cc;

    private Agent jadeAgent;

    JadeRuntimeServices(ContainerController cc, Agent ag) {
        this.cc = cc;
        jadeAgent = ag;
    }

    @Override
    public String getNewAgentName(String baseName) {
        return baseName;
    }

    @Override
    public boolean isRunning() {
        return cc != null;
    }

    @Override
    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts, jason.asSemantics.Agent father) throws Exception {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Creating jade agent " + agName + "from source " + agSource + "(agClass=" + agClass + ", archClass=" + archClasses + ", settings=" + stts);
            }

            AgentParameters ap = new AgentParameters();
            ap.setAgClass(agClass);
            ap.addArchClass(archClasses);
            ap.setBB(bbPars);
            ap.setSource(agSource);

            if (stts == null) stts = new Settings();
            agName = getNewAgentName(agName);
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

    public Collection<String> getAgentsNames() {
        // TODO: make a cache list and update it when a new agent enters the system
        if (jadeAgent == null) return null;
        try {
            Set<String> ags = new HashSet<>();
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

    public boolean killAgent(String agName, String byAg, int deadline) {
        // TODO: implement deadline for JADE
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


    public void stopMAS(int deadline, boolean stopJVM, int exitValue) throws Exception {
        if (cc != null) {
            new Thread() { // this command should not block the agent!
                public void run() {
                    try {
                        cc.getPlatformController().kill();
                        if (stopJVM)
                            System.exit(exitValue);
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                }
            } .start();
            cc = null;
        }
    }

    @Override
    public void dfRegister(String agName, String service, String type) {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( jadeAgent.getAID() );

            DFAgentDescription[] result = DFService.search(jadeAgent, dfd);
            if (result.length>0) {
                // copy current services
                @SuppressWarnings("unchecked")
                Iterator<ServiceDescription> i = result[0].getAllServices();
                while (i.hasNext()) {
                    dfd.addServices(i.next());
                }

                DFService.deregister(jadeAgent);
            }

            // add new service
            ServiceDescription sd  = new ServiceDescription();
            sd.setType( type );
            sd.setName( service );
            dfd.addServices(sd);

            DFService.register(jadeAgent, dfd );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dfDeRegister(String agName, String service, String type) {
        try {
            // removes only service
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( jadeAgent.getAID() );

            DFAgentDescription[] result = DFService.search(jadeAgent, dfd);
            if (result.length>0) {
                // copy current services
                @SuppressWarnings("unchecked")
                Iterator<ServiceDescription> i = result[0].getAllServices();
                while (i.hasNext()) {
                    if (!i.next().toString().contains(service))
                        dfd.addServices(i.next());
                }

                DFService.deregister(jadeAgent);
            }

            DFService.register(jadeAgent, dfd );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<String> dfSearch(String service, String type) {
        List<String> r = new ArrayList<>();
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd  = new ServiceDescription();
            sd.setType( type );
            sd.setName( service );
            dfd.addServices(sd);

            DFAgentDescription[] result = DFService.search(jadeAgent, dfd);

            for (int i=0; i<result.length; i++) {
                r.add(result[i].getName().getLocalName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public void dfSubscribe(String agName, String service, String type) {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType( type );
            sd.setName( service );
            dfd.addServices(sd);

            jadeAgent.send(DFService.createSubscriptionMessage(jadeAgent, jadeAgent.getDefaultDF(), dfd, new SearchConstraints()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Collection<String> defaultAgArchs = new ArrayList<>();
    @Override
    public void registerDefaultAgArch(String agArch) {
        defaultAgArchs.add(agArch);
    }
    @Override
    public Collection<String> getDefaultAgArchs() {
        return defaultAgArchs;
    }

}
