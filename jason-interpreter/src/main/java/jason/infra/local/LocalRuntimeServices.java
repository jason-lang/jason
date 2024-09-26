package jason.infra.local;

import jason.JasonException;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.pl.PlanLibrary;
import jason.runtime.RuntimeServicesFactory;
import jason.runtime.Settings;
import jason.runtime.SourcePath;
import jason.stdlib.print_unifier;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

    private Lock createLock = new ReentrantLock();

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

        createLock.lock();
        try { // to avoid problems related to concurrent executions of .create_agent
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
        } finally {
            createLock.unlock();
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
            ag.getPL().getLock().lock();
            try {
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
            } finally {
                ag.getPL().getLock().unlock();
            }
            return "ok";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String runAsAgent(String agName, String code) {
        try {
            LocalAgArch agArch = masRunner.getAg(agName);
            if (agArch == null)
                return "no agent named "+agName;

            code = code.trim();
            if (code.endsWith("."))
                code = code.substring(0,code.length()-1);
            while (code.endsWith(";"))
                code = code.substring(0,code.length()-1);

            code += "; "+ print_unifier.class.getName();
            PlanBody lCmd = ASSyntax.parsePlanBody(code);

//            parent.parent.println(lCmd.getBodyNext()+" -- "+lCmd.getBodyNext().getBodyType().getClass().getName());
            var te   = ASSyntax.parseTrigger("+!run_repl_expr");
            var i    = new Intention();
            var plan =  new Plan(null,te,null,lCmd);
            i.push(new IntendedMeans(
                    new Option(
                            plan,
                            new Unifier()),
                    te));

            agArch.getTS().getC().addRunningIntention(i);

//            parent.parent.println("------" + ag.getTS().getAg().getPL().getAsTxt(false));
//            parent.parent.println("------" + ag.getTS().getC().getRunningIntentions());
//            parent.parent.println("------" + ag.getTS().getAg().getPL().getCandidatePlans(
//                    ASSyntax.parseTrigger("+!a")
//            ));

            agArch.getTS().getAgArch().wake();


        } catch (Exception e) {
            return "Error parsing "+code+"\n"+e;
        }
        return "execution ok";
    }
}

