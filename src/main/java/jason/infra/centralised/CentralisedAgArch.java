package jason.infra.centralised;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;
import jason.util.Config;

/**
 * This class provides an agent architecture when using Centralised
 * infrastructure to run the MAS inside Jason.
 *
 * Each agent has its own thread.
 *
 * <p>
 * Execution sequence:
 * <ul>
 * <li>initAg,
 * <li>setEnvInfraTier,
 * <li>setControlInfraTier,
 * <li>run (perceive, checkMail, act),
 * <li>stopAg.
 * </ul>
 */
public class CentralisedAgArch extends AgArch implements Runnable {

    protected CentralisedEnvironment    infraEnv     = null;
    private CentralisedExecutionControl infraControl = null;
    private BaseCentralisedMAS           masRunner    = BaseCentralisedMAS.getRunner();

    private String           agName  = "";
    private volatile boolean running = true;
    private Queue<Message>   mbox    = new ConcurrentLinkedQueue<Message>();
    protected Logger         logger  = Logger.getLogger(CentralisedAgArch.class.getName());

    private static List<MsgListener> msgListeners = null;
    public static void addMsgListener(MsgListener l) {
        if (msgListeners == null) {
            msgListeners = new ArrayList<MsgListener>();
        }
        msgListeners.add(l);
    }
    public static void removeMsgListener(MsgListener l) {
        msgListeners.remove(l);
    }

    /**
     * Creates the user agent architecture, default architecture is
     * jason.architecture.AgArch. The arch will create the agent that creates
     * the TS.
     */
    public void createArchs(List<String> agArchClasses, String agClass, ClassParameters bbPars, String asSrc, Settings stts, BaseCentralisedMAS masRunner) throws JasonException {
        try {
            this.masRunner = masRunner;
            Agent.create(this, agClass, bbPars, asSrc, stts);
            insertAgArch(this);

            createCustomArchs(agArchClasses);

            // mind inspector arch
            if (stts.getUserParameter(Settings.MIND_INSPECTOR) != null) {
                insertAgArch( (AgArch)Class.forName( Config.get().getMindInspectorArchClassName()).newInstance() );
                getFirstAgArch().init();
            }

            setLogger();
        } catch (Exception e) {
            running = false;
            throw new JasonException("as2j: error creating the agent class! - "+e.getMessage(), e);
        }
    }

    /** init the agent architecture based on another agent */
    public void createArchs(List<String> agArchClasses, Agent ag, BaseCentralisedMAS masRunner) throws JasonException {
        try {
            this.masRunner = masRunner;
            setTS(ag.clone(this).getTS());
            insertAgArch(this);

            createCustomArchs(agArchClasses);

            setLogger();
        } catch (Exception e) {
            running = false;
            throw new JasonException("as2j: error creating the agent class! - ", e);
        }
    }


    public void stopAg() {
        running = false;
        wake(); // so that it leaves the run loop
        if (myThread != null)
            myThread.interrupt();
        getTS().getAg().stopAg();
        getUserAgArch().stop(); // stops all archs
    }


    public void setLogger() {
        logger = Logger.getLogger(CentralisedAgArch.class.getName() + "." + getAgName());
        if (getTS().getSettings().verbose() >= 0)
            logger.setLevel(getTS().getSettings().logLevel());
    }

    public Logger getLogger() {
        return logger;
    }

    public void setAgName(String name) throws JasonException {
        if (name.equals("self"))
            throw new JasonException("an agent cannot be named 'self'!");
        if (name.equals("percept"))
            throw new JasonException("an agent cannot be named 'percept'!");
        agName = name;
    }

    public String getAgName() {
        return agName;
    }

    public AgArch getUserAgArch() {
        return getFirstAgArch();
    }

    public void setEnvInfraTier(CentralisedEnvironment env) {
        infraEnv = env;
    }

    public CentralisedEnvironment getEnvInfraTier() {
        return infraEnv;
    }

    public void setControlInfraTier(CentralisedExecutionControl pControl) {
        infraControl = pControl;
    }

    public CentralisedExecutionControl getControlInfraTier() {
        return infraControl;
    }

    private Thread myThread = null;
    public void setThread(Thread t) {
        myThread = t;
        myThread.setName(agName);
    }

    public void startThread() {
        myThread.start();
    }

    public boolean isRunning() {
        return running;
    }

    protected void sense() {
        TransitionSystem ts = getTS();

        int i = 0;
        do {
            ts.sense(); // must run at least once, so that perceive() is called
        } while (running && ++i < cyclesSense && !ts.canSleepSense());
    }

    //int sumDel = 0; int nbDel = 0;
    protected void deliberate() {
        TransitionSystem ts = getTS();
        int i = 0;
        while (running && i++ < cyclesDeliberate && !ts.canSleepDeliberate()) {
            ts.deliberate();
        }
        //sumDel += i; nbDel++;
        //System.out.println("running del "+(sumDel/nbDel)+"/"+cyclesDeliberate);
    }

    //int sumAct = 0; int nbAct = 0;
    protected void act() {
        TransitionSystem ts = getTS();

        int i = 0;
        int ca = cyclesAct;
        if (cyclesAct == 9999)
            ca = ts.getC().getIntentions().size();

        while (running && i++ < ca && !ts.canSleepAct()) {
            ts.act();
        }
        //sumAct += i; nbAct++;
        //System.out.println("running act "+(sumAct/nbAct)+"/"+ca);
    }

    protected void reasoningCycle() {
        sense();
        deliberate();
        act();
    }

    public void run() {
        TransitionSystem ts = getTS();
        while (running) {
            if (ts.getSettings().isSync()) {
                waitSyncSignal();
                reasoningCycle();
                boolean isBreakPoint = false;
                try {
                    isBreakPoint = ts.getC().getSelectedOption().getPlan().hasBreakpoint();
                    if (logger.isLoggable(Level.FINE)) logger.fine("Informing controller that I finished a reasoning cycle "+getCycleNumber()+". Breakpoint is " + isBreakPoint);
                } catch (NullPointerException e) {
                    // no problem, there is no sel opt, no plan ....
                }
                informCycleFinished(isBreakPoint, getCycleNumber());
            } else {
                incCycleNumber();
                reasoningCycle();
                if (ts.canSleep())
                    sleep();
            }
        }
        logger.fine("I finished!");
    }

    private Object sleepSync = new Object();
    private int    sleepTime = 50;

    public static final int MAX_SLEEP = 1000;

    public void sleep() {
        try {
            if (!getTS().getSettings().isSync()) {
                //logger.fine("Entering in sleep mode....");
                synchronized (sleepSync) {
                    sleepSync.wait(sleepTime); // wait for messages
                    if (sleepTime < MAX_SLEEP)
                        sleepTime += 100;
                }
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.WARNING,"Error in sleep.", e);
        }
    }

    @Override
    public void wake() {
        synchronized (sleepSync) {
            sleepTime = 50;
            sleepSync.notifyAll(); // notify sleep method
        }
    }

    @Override
    public void wakeUpSense() {
        wake();
    }

    @Override
    public void wakeUpDeliberate() {
        wake();
    }

    @Override
    public void wakeUpAct() {
        wake();
    }

    // Default perception assumes Complete and Accurate sensing.
    @Override
    public Collection<Literal> perceive() {
        super.perceive();
        if (infraEnv == null) return null;
        Collection<Literal> percepts = infraEnv.getUserEnvironment().getPercepts(getAgName());
        if (logger.isLoggable(Level.FINE) && percepts != null) logger.fine("percepts: " + percepts);
        return percepts;
    }

    // this is used by the .send internal action in stdlib
    public void sendMsg(Message m) throws ReceiverNotFoundException {
        // actually send the message
        if (m.getSender() == null)  m.setSender(getAgName());

        CentralisedAgArch rec = masRunner.getAg(m.getReceiver());

        if (rec == null) {
            if (isRunning())
                throw new ReceiverNotFoundException("Receiver '" + m.getReceiver() + "' does not exist! Could not send " + m);
            else
                return;
        }
        rec.receiveMsg(m.clone()); // send a cloned message

        // notify listeners
        if (msgListeners != null)
            for (MsgListener l: msgListeners)
                l.msgSent(m);
    }

    public void receiveMsg(Message m) {
        mbox.offer(m);
        wakeUpSense();
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
        for (String agName: masRunner.getAgs().keySet()) {
            if (!agName.equals(this.getAgName())) {
                m.setReceiver(agName);
                sendMsg(m);
            }
        }
    }

    // Default procedure for checking messages, move message from local mbox to C.mbox
    public void checkMail() {
        Circumstance C = getTS().getC();
        Message im = mbox.poll();
        while (im != null) {
            C.addMsg(im);
            if (logger.isLoggable(Level.FINE)) logger.fine("received message: " + im);
            im = mbox.poll();
        }
    }

    public Collection<Message> getMBox() {
        return mbox;
    }

    /** called by the TS to ask the execution of an action in the environment */
    @Override
    public void act(ActionExec action) {
        //if (logger.isLoggable(Level.FINE)) logger.fine("doing: " + action.getActionTerm());

        if (isRunning()) {
            if (infraEnv != null) {
                infraEnv.act(getAgName(), action);
            } else {
                action.setResult(false);
                action.setFailureReason(new Atom("noenv"), "no environment configured!");
                actionExecuted(action);
            }
        }
    }

    public boolean canSleep() {
        return mbox.isEmpty() && isRunning();
    }

    private Object  syncMonitor                = new Object();
    private volatile boolean inWaitSyncMonitor = false;

    /**
     * waits for a signal to continue the execution (used in synchronised
     * execution mode)
     */
    private void waitSyncSignal() {
        try {
            synchronized (syncMonitor) {
                inWaitSyncMonitor = true;
                syncMonitor.wait();
                inWaitSyncMonitor = false;
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.WARNING,"Error waiting sync (1)", e);
        }
    }

    /**
     * inform this agent that it can continue, if it is in sync mode and
     * waiting a signal
     */
    public void receiveSyncSignal() {
        try {
            synchronized (syncMonitor) {
                while (!inWaitSyncMonitor && isRunning()) {
                    // waits the agent to enter in waitSyncSignal
                    syncMonitor.wait(50);
                }
                syncMonitor.notifyAll();
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.WARNING,"Error waiting sync (2)", e);
        }
    }

    /**
     *  Informs the infrastructure tier controller that the agent
     *  has finished its reasoning cycle (used in sync mode).
     *
     *  <p><i>breakpoint</i> is true in case the agent selected one plan
     *  with the "breakpoint" annotation.
     */
    public void informCycleFinished(boolean breakpoint, int cycle) {
        infraControl.receiveFinishedCycle(getAgName(), breakpoint, cycle);
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(masRunner);
    }

    private RConf conf;

    private int cycles = 1;

    private int cyclesSense      = 1;
    private int cyclesDeliberate = 1;
    private int cyclesAct        = 5;

    public void setConf(RConf conf) {
        this.conf = conf;
    }

    public RConf getConf() {
        return conf;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public int getCyclesSense() {
        return cyclesSense;
    }

    public void setCyclesSense(int cyclesSense) {
        this.cyclesSense = cyclesSense;
    }

    public int getCyclesDeliberate() {
        return cyclesDeliberate;
    }

    public void setCyclesDeliberate(int cyclesDeliberate) {
        this.cyclesDeliberate = cyclesDeliberate;
    }

    public int getCyclesAct() {
        return cyclesAct;
    }

    public void setCyclesAct(int cyclesAct) {
        this.cyclesAct = cyclesAct;
    }

}
