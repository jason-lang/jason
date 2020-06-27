package jason.infra.centralised;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.management.NotificationBroadcasterSupport;

import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.UnnamedVar;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServices;
import jason.runtime.RuntimeServicesFactory;

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

    protected AgArch dfAg = null;

    public boolean isDebug() {
        return debug;
    }

    public static BaseCentralisedMAS getRunner() {
        return runner;
    }

    /**
     * @deprecated use RuntimeServicesFactory.get() instead.  
     */
    public RuntimeServices getRuntimeServices() {
        return RuntimeServicesFactory.get();
    }

    /**
     * @deprecated use RuntimeServicesFactory.set() instead.  
     */
    public void setRuntimeServives(RuntimeServices rts) {
        RuntimeServicesFactory.set(rts);
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
        ag.setMASRunner(this);
    }
    public CentralisedAgArch delAg(String agName) {
        //df.remove(agName);
        try {
            getDFAg().abolish(ASSyntax.createLiteral("provider",  new Atom(agName), new UnnamedVar()), null);
            getDFAg().abolish(ASSyntax.createLiteral("subscribe", new Atom(agName), new UnnamedVar()), null);
        } catch (RevisionFailedException e) {
            e.printStackTrace();
        }
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

    public abstract void finish(int deadline, boolean stopJVM);
    public void finish() { finish(0, true); }

    public abstract boolean hasDebugControl();

    public abstract void enableDebugControl();

    public abstract boolean isRunning();


    protected synchronized AgArch getDFAgArch() {
        if (dfAg == null) {
            try {
                String name = RuntimeServicesFactory.get().createAgent("df", null, null, null, null, null, null);
                RuntimeServicesFactory.get().startAgent(name);
                dfAg = getAg(name).getFirstAgArch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dfAg;
    }
    
    protected Agent getDFAg() {
        return getDFAgArch().getTS().getAg();
    }

    protected Collection<Literal> getSubscribers() {
        Collection<Literal> a = new ArrayList<>();
        Iterator<Literal> ibb = getDFAg().getBB().getCandidateBeliefs(new PredicateIndicator("subscribe", 2));
        if (ibb != null) {
            while (ibb.hasNext()) {
                a.add(ibb.next());
            }
        }
        return a;
    }
    
    /** DF methods  **/

    public void dfRegister(String agName, String service) {
        try {
            getDFAg().addBel( ASSyntax.createLiteral("provider", new Atom(agName), new Atom(service)));
            
            // inform subscribers
            for (Literal p: getSubscribers()) {
                if (p.getTerm(1).toString().equals(service))
                    sendProvider(p.getTerm(0).toString(), agName, service);
            }
        } catch (RevisionFailedException e) {
            e.printStackTrace();
        }
    }

    public void dfDeRegister(String agName, String service) {
        try {
            getDFAg().delBel( ASSyntax.createLiteral("provider", new Atom(agName), new Atom(service)));
        } catch (RevisionFailedException e) {
            e.printStackTrace();
        }
    }

    public Collection<String> dfSearch(String service) {
        Set<String> ags = new HashSet<>();
        Map<String, Set<String>> df = getDF();
        for (String ag: df.keySet()) {
            for (String l: df.get(ag)) {
                if (l.equals(service)) {
                    ags.add(ag);
                }
            }
        }
        return ags;
    }

    public void dfSubscribe(String agName, String service) {
        try {
            getDFAg().addBel( ASSyntax.createLiteral("subscribe", new Atom(agName), new Atom(service)));
            // sends it all current providers
            for (String a: dfSearch(service))
                sendProvider(agName,a,service);
        } catch (RevisionFailedException e) {
            e.printStackTrace();
        }
    }
    
    protected void sendProvider(String receiver, String provider, String service) {
        try {
            Message m = new Message("tell", getDFAgArch().getAgName(), receiver, ASSyntax.createLiteral("provider", new Atom(provider), new StringTermImpl(service)));
            getDFAgArch().sendMsg(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Set<String>> getDF() {
        Map<String, Set<String>> a = new HashMap<>();

        Iterator<Literal> ibb = getDFAg().getBB().getCandidateBeliefs(new PredicateIndicator("provider", 2));
        if (ibb != null) {
            while (ibb.hasNext()) {
                Literal p = ibb.next();
                String ag = p.getTerm(0).toString();
                Set<String> services = a.computeIfAbsent(ag, k -> new HashSet<>());
                services.add(p.getTerm(1).toString());
                a.put(ag, services);
            }
        }
        
        return a;
    }
}
