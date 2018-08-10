package jason.infra.centralised;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;
import jason.runtime.SourcePath;

/** This class implements the centralised version of the runtime services. */
public class CentralisedRuntimeServices implements RuntimeServicesInfraTier {

    private static Logger logger = Logger.getLogger(CentralisedRuntimeServices.class.getName());

    protected BaseCentralisedMAS masRunner;

    public CentralisedRuntimeServices(BaseCentralisedMAS masRunner) {
        this.masRunner = masRunner;
    }

    protected CentralisedAgArch newAgInstance() {
        return new CentralisedAgArch();
    }

    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception {
        if (logger.isLoggable(Level.FINE))
            logger.fine("Creating centralised agent " + agName + " from source " + agSource + " (agClass=" + agClass + ", archClass=" + archClasses + ", settings=" + stts);

        AgentParameters ap = new AgentParameters();
        ap.setAgClass(agClass);
        ap.addArchClass(archClasses);
        ap.setBB(bbPars);

        if (stts == null)
            stts = new Settings();

        String prefix = null;
        if (father != null && father.getASLSrc().startsWith(SourcePath.CRPrefix))
            prefix = SourcePath.CRPrefix + "/";
        agSource = masRunner.getProject().getSourcePaths().fixPath(agSource, prefix);

        String nb = "";
        synchronized (logger) { // to avoid problems related to concurrent executions of .create_agent
            int n = 1;
            while (masRunner.getAg(agName+nb) != null)
                nb = "_" + (n++);
            agName = agName + nb;

            CentralisedAgArch agArch = newAgInstance();
            agArch.setAgName(agName);
            agArch.createArchs(ap.getAgArchClasses(), ap.agClass.getClassName(), ap.getBBClass(), agSource, stts, masRunner);
            agArch.setEnvInfraTier(masRunner.getEnvironmentInfraTier());
            agArch.setControlInfraTier(masRunner.getControllerInfraTier());
            
            // if debug mode is active, set up new agent to be synchronous and visible for ExecutionControlGUI
            if (masRunner.isDebug()) {
                stts.setVerbose(2);
                stts.setSync(true);
                agArch.getLogger().setLevel(Level.FINE);
                agArch.getTS().getLogger().setLevel(Level.FINE);
                agArch.getTS().getAg().getLogger().setLevel(Level.FINE);
            }

            masRunner.addAg(agArch);
        }

        logger.fine("Agent " + agName + " created!");
        return agName;
    }

    public void startAgent(String agName) {
        // create the agent thread
        CentralisedAgArch agArch = masRunner.getAg(agName);
        Thread agThread = new Thread(agArch);
        agArch.setThread(agThread);
        agThread.start();
    }

    public AgArch clone(Agent source, List<String> archClasses, String agName) throws JasonException {
        // create a new infra arch
        CentralisedAgArch agArch = newAgInstance();
        agArch.setAgName(agName);
        agArch.setEnvInfraTier(masRunner.getEnvironmentInfraTier());
        agArch.setControlInfraTier(masRunner.getControllerInfraTier());
        masRunner.addAg(agArch);

        agArch.createArchs(archClasses, source, masRunner);

        startAgent(agName);
        return agArch.getUserAgArch();
    }

    public Set<String> getAgentsNames() {
        return masRunner.getAgs().keySet();
    }

    public int getAgentsQty() {
        return masRunner.getAgs().keySet().size();
    }

    public boolean killAgent(String agName, String byAg) {
        logger.fine("Killing centralised agent " + agName);
        CentralisedAgArch ag = masRunner.getAg(agName);
        if (ag != null && ag.getTS().getAg().killAcc(byAg)) {
            ag.stopAg();
            masRunner.delAg(agName);
            return true;
        }
        return false;
    }

    public void stopMAS() throws Exception {
        masRunner.finish();
    }
}
