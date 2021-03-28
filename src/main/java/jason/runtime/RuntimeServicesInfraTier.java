package jason.runtime;

import java.util.Collection;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.ClassParameters;

@Deprecated
public interface RuntimeServicesInfraTier extends RuntimeServices {

    default public String createAgent(String agName, String agSource, String agClass, Collection<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception {
        return null;
    };

    default public String getNewAgentName(String baseName) { return null; }

    default public void startAgent(String agName) {};

    default public AgArch clone(Agent source, Collection<String> archClasses, String agName) throws JasonException { return null; }
    default public boolean killAgent(String agName, String byAg, int deadline) { return false; }

    default public Collection<String> getAgentsNames() {return null; }
    default public int getAgentsQty() { return 0; }
    default public boolean isRunning() {return false; }
    default public void stopMAS(int deadline, boolean stopJVM, int exitValue) throws Exception {};
}
