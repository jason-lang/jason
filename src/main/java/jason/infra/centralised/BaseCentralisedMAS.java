package jason.infra.centralised;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.management.NotificationBroadcasterSupport;

import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

/**
 * Runs MASProject using centralised infrastructure.
 */
public abstract class BaseCentralisedMAS extends NotificationBroadcasterSupport {

    public final static String       logPropFile     = "logging.properties";
    public final static String       stopMASFileName = ".stop___MAS";
    public final static String       defaultProjectFileName = "default.mas2j";

    protected static Logger             logger        = Logger.getLogger(BaseCentralisedMAS.class.getName());
    protected static BaseCentralisedMAS runner        = null;
    protected static String             urlPrefix     = "";
    protected static boolean            readFromJAR   = false;
    protected static MAS2JProject       project;
    protected static boolean            debug         = false;

    protected CentralisedEnvironment        env         = null;
    protected CentralisedExecutionControl   control     = null;
    protected Map<String,CentralisedAgArch> ags         = new ConcurrentHashMap<String,CentralisedAgArch>();

    public boolean isDebug() {
        return debug;
    }

    public static BaseCentralisedMAS getRunner() {
        return runner;
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(runner);
    }

    public CentralisedExecutionControl getControllerInfraTier() {
        return control;
    }

    public CentralisedEnvironment getEnvironmentInfraTier() {
        return env;
    }

    public MAS2JProject getProject() {
        return project;
    }
    public void setProject(MAS2JProject p) {
        project = p;
    }

    public void addAg(CentralisedAgArch ag) {
        ags.put(ag.getAgName(), ag);
    }
    public CentralisedAgArch delAg(String agName) {
        return ags.remove(agName);
    }

    public CentralisedAgArch getAg(String agName) {
        return ags.get(agName);
    }

    public Map<String,CentralisedAgArch> getAgs() {
        return ags;
    }

    public int getNbAgents() {
        return ags.size();
    }
    
    public abstract void setupLogger();

    public abstract void finish();

    public abstract boolean hasDebugControl();

    public abstract void enableDebugControl();

}
