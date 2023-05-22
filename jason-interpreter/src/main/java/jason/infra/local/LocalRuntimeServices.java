package jason.infra.local;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Trigger;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.pl.PlanLibrary;
import jason.runtime.RuntimeServicesFactory;
import jason.runtime.Settings;
import jason.runtime.SourcePath;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** This class implements the Local version of the runtime services. */
public class LocalRuntimeServices extends BaseRuntimeServices {

    private static final Logger logger = Logger.getLogger(LocalRuntimeServices.class.getName());

    public LocalRuntimeServices(BaseLocalMAS masRunner) {
        super(masRunner);
    }

    @Override public boolean isRunning() {
        return masRunner.isRunning();
    }

    protected LocalAgArch newAgInstance() {
        return new LocalAgArch();
    }

    @Override
    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception {
        if (!isRunning())
            return "system.not.running";

        if (logger.isLoggable(Level.FINE))
            logger.fine("Creating local agent " + agName + " from source " + agSource + " (agClass=" + agClass + ", archClass=" + archClasses + ", settings=" + stts);

        AgentParameters ap = new AgentParameters();
        ap.setAgClass(agClass);
        ap.setBB(bbPars);
        if (archClasses != null && !archClasses.isEmpty()) {
            ap.addArchClass(archClasses);
        } else {
            if (father != null) {
                // use father agArchs
                ap.addArchClass(father.getTS().getAgArch().getAgArchClassesChain());
            } else {
                // use default agArch
                ap.addArchClass(RuntimeServicesFactory.get().getDefaultAgArchs());
            }
        }

        if (stts == null)
            stts = new Settings();

        if (father != null && father.getASLSrc() != null && father.getASLSrc().startsWith(SourcePath.CRPrefix))
            masRunner.getProject().getSourcePaths().addPath(SourcePath.CRPrefix);

        agSource = masRunner.getProject().getSourcePaths().fixPath(agSource);

        synchronized (logger) { // to avoid problems related to concurrent executions of .create_agent
            agName = getNewAgentName(agName);

            LocalAgArch agArch = newAgInstance();
            agArch.setAgName(agName);
            agArch.createArchs(ap.getAgArchClasses(), ap.agClass.getClassName(), ap.getBBClass(), agSource, stts);
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

    @Override
    public void startAgent(String agName) {
        // TODO: implement the proper start in case of using pool of threads
        // create the agent thread
        LocalAgArch agArch = masRunner.getAg(agName);
        agArch.setThread(new Thread(agArch));
        agArch.startThread();
    }

    @Override
    public void clone(Agent source, List<String> archClasses, String agName) throws JasonException {
        // create a new infra arch
        LocalAgArch agArch = newAgInstance();
        agArch.setAgName(agName);
        agArch.setEnvInfraTier(masRunner.getEnvironmentInfraTier());
        agArch.setControlInfraTier(masRunner.getControllerInfraTier());
        masRunner.addAg(agArch);

        agArch.createArchs(archClasses, source);

        startAgent(agName);
        //return agArch.getFirstAgArch();
    }

    @Override
    public boolean killAgent(String agName, String byAg, int deadline) {
        logger.fine("Killing local agent " + agName);
        LocalAgArch ag = masRunner.getAg(agName);
        if (ag != null && ag.getTS().getAg().killAcc(byAg)) {
            new Thread(() -> {
                if (deadline != 0) {
                    // gives some time for the agent
                    Trigger te = PlanLibrary.TE_JAG_SHUTTING_DOWN.clone();
                    te.getLiteral().addTerm(new NumberTermImpl(deadline));
                    ag.getTS().getC().addExternalEv(te);

                    try {
                        Thread.sleep(deadline);
                    } catch (InterruptedException e) {                      }
                }
                ag.stopAg();
                masRunner.delAg(agName);
            }).start();
            return true;
        }
        return false;
    }

    @Override
    public String getMASName() {
        try {
            return masRunner.getProject().getSocName();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public Map<String, Object> getAgStatus(String agName) {
        LocalAgArch ag = masRunner.getAg(agName);
        if (ag != null) {
            return ag.getStatus();
        } else {
            return null;
        }
    }

    @Override
    public Agent getAgentSnapshot(String agName) {
        LocalAgArch ag = masRunner.getAg(agName);
        if (ag == null)
            return null;

        return ag.getTS().getAg().clone(ag);
    }

    @Override
    public String loadASL(String agName, String code, String sourceId, boolean replace) {
        LocalAgArch agArch = masRunner.getAg(agName);
        if (agArch == null)
            return "no agent named "+agName;

        try {
            var ag = agArch.getTS().getAg();
            synchronized (ag.getPL().getLock()) {
                if (replace) {
                    var toRem = new ArrayList<Pred>();
                    for (var p : ag.getPL()) {
                        if (p.getSourceFile().equals(sourceId)) {
                            toRem.add(p.getLabel());
                        }
                    }
                    for (var l : toRem) {
                        ag.getPL().remove(l);
                    }
                }
                ag.parseAS(new StringReader(code), sourceId);
                ag.addInitialBelsInBB();
                ag.addInitialGoalsInTS();
            }
            return "ok";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

