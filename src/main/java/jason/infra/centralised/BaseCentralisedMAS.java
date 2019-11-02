package jason.infra.centralised;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.management.NotificationBroadcasterSupport;

import jason.asSemantics.Message;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.StringTermImpl;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServices;
import jason.util.Pair;

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
    protected Map<String,CentralisedAgArch> ags         = new ConcurrentHashMap<>();

    protected Map<String, Set<String>>     df = new HashMap<>();
    protected List<Pair<String, String>>    subscribers = new ArrayList<>();

    public boolean isDebug() {
        return debug;
    }

    public static BaseCentralisedMAS getRunner() {
        return runner;
    }

    protected RuntimeServices singRTS = null;

    public RuntimeServices getRuntimeServices() {
        if (singRTS == null)
            singRTS = new CentralisedRuntimeServices(runner);
        return singRTS;
    }

    public void setRuntimeServives(RuntimeServices rts) {
        singRTS = rts;
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
        df.remove(agName);
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

    public abstract void finish(int deadline);
    public void finish() { finish(0); }

    public abstract boolean hasDebugControl();

    public abstract void enableDebugControl();


    /** DF methods **/

    public void dfRegister(String agName, String service) {
        synchronized (df) {
            Set<String> s = df.get(agName);
            if (s == null)
                s = new HashSet<>();
            s.add(service);
            df.put(agName, s);

            // inform subscribers
            for (Pair<String,String> p: subscribers) {
                if (p.getSecond().equals(service))
                    sendProvider(p.getFirst(), agName, service);
            }
        }
    }

    public void dfDeRegister(String agName, String service) {
        synchronized (df) {
            Set<String> s = df.get(agName);
            if (s == null)
                return;
            else
                s.remove(service);
        }
    }

    public Collection<String> dfSearch(String service) {
        synchronized (df) {
            Set<String> ags = new HashSet<>();
            for (String ag: df.keySet()) {
                for (String l: df.get(ag)) {
                    if (l.equals(service)) {
                        ags.add(ag);
                    }
                }
            }
            return ags;
        }
    }

    public void dfSubscribe(String agName, String service) {
        synchronized (df) {
            // sends him all current providers
            for (String a: dfSearch(service))
                sendProvider(agName,a,service);
            // register as interested
            subscribers.add(new Pair<>(agName, service));
        }
    }

    protected void sendProvider(String receiver, String provider, String service) {
        Message m = new Message("tell", "df", receiver, ASSyntax.createLiteral("provider", new Atom(provider), new StringTermImpl(service)));
        getAg(receiver).receiveMsg(m);
    }

    public Map<String, Set<String>> getDF() {
            return df;
    }
}
