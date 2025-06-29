package jason.architecture;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;

import jason.asSemantics.ActionExec;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.infra.local.LocalAgArch;
import jason.runtime.RuntimeServices;

/**
 * Base agent architecture class that defines the overall agent architecture;
 * the AS interpreter is the reasoner (a kind of mind) within this
 * architecture (a kind of body).
 *
 * <p>
 * The agent reasoning cycle (implemented in TransitionSystem class) calls these
 * methods to get perception, action, and communication.
 *
 * <p>
 * This class implements a Chain of Responsibilities design pattern.
 * Each member of the chain is a subclass of AgArch. The last arch in the chain is the infrastructure tier (Local, JADE, Saci, ...).
 * The getFirstAgArch method returns the first arch in the chain.
 *
 * Users can customise the architecture in a sub class of AgArch and
 * overriding some methods.
 */
public class AgArch implements Comparable<AgArch>, Serializable {

    private static final long serialVersionUID = 1L;

    private TransitionSystem ts = null;

    /**
     * Successor in the Chain of Responsibility
     */
    private AgArch successor = null;
    private AgArch firstArch = null;

    /** the current cycle number, in case of sync execution mode */
    private int cycleNumber = 0;

    public AgArch() {
        firstArch = this;
    }

    public void init() throws Exception {
    }

    /**
     * A call-back method called by the infrastructure tier
     * when the agent is about to be killed.
     */
    public void stop() {
        if (successor != null)
            successor.stop();
    }


    // Management of the chain of responsibility
    /** Returns the first architecture in the chain of responsibility pattern */
    public AgArch getFirstAgArch() {
        return firstArch;
    }
    public AgArch getNextAgArch() {
        return successor;
    }
    public List<String> getAgArchClassesChain() {
        List<String> all = new ArrayList<>();
        AgArch a = getFirstAgArch();
        while (a != null) {
            all.add(a.getClass().getName());
            a = a.getNextAgArch();
        }
        return all;
    }

    public void insertAgArch(AgArch arch) {
        if (arch != firstArch) // to avoid loops
            arch.successor = firstArch;
        if (ts != null) {
            arch.ts = this.ts;
            ts.setAgArch(arch);
        }
        setFirstAgArch(arch);
    }
    private void setFirstAgArch(AgArch arch) {
        firstArch = arch;
        if (successor != null)
            successor.setFirstAgArch(arch);
    }

    public void createCustomArchs(List<String> archs) throws Exception {
        if (archs == null)
            return;
        for (int i=archs.size()-1; i>=0; i--) {
            var agArchClass = archs.get(i);
            // user custom arch
            if (!agArchClass.isEmpty() && !agArchClass.equals(AgArch.class.getName()) && !agArchClass.equals(LocalAgArch.class.getName())) {
                try {
                    AgArch a = (AgArch) Class.forName(agArchClass).getConstructor().newInstance();
                    a.setTS(ts); // so a.init() can use TS
                    insertAgArch(a);
                    a.init();
                    //System.out.println("creating arch "+agArchClass+ " "+getTS().getAgArch().getAgArchClassesChain());
                } catch (Exception e) {
                    System.out.println("Error creating custom agent architecture (class='"+agArchClass+"')."+e);
                    e.printStackTrace();
                    ts.getLogger().log(Level.SEVERE,"Error creating custom agent architecture (class='"+agArchClass+"').", e);
                }
            }
        }
    }

    /**
     * A call-back method called by TS
     * when a new reasoning cycle is starting
     */
    public void reasoningCycleStarting() {
        //QueryProfiling q = getTS().getAg().getQueryProfiling();
        //if (q != null)
        //    q.setNbReasoningCycles(getCycleNumber());
        if (successor != null)
            successor.reasoningCycleStarting();
    }

    /**
     * A call-back method called by TS
     * when a new reasoning cycle finished
     */
    public void reasoningCycleFinished() {
        if (successor != null)
            successor.reasoningCycleFinished();
    }


    public TransitionSystem getTS() {
        if (ts != null)
            return ts;
        if (successor != null)
            return successor.getTS();
        return null;
    }

    public void setTS(TransitionSystem ts) {
        this.ts = ts;
        if (successor != null)
            successor.setTS(ts);
    }

    /** Gets the agent's perception as a list of Literals.
     *  The returned list will be modified by Jason.
     */
    public Collection<Literal> perceive() {
        if (successor == null)
            return null;
        else
            return successor.perceive();
    }

    /** Reads the agent's mailbox and adds messages into
        the agent's circumstance */
    public void checkMail() {
        if (successor != null)
            successor.checkMail();
    }

    /**
     * Executes the action <i>action</i> and, when finished, adds it back in
     * <i>feedback</i> actions.
     *
     * @return true if the action was handled (not necessarily executed, just started)
     */
    public void act(ActionExec action) {
        if (successor != null)
            successor.act(action);
    }

    /** called to inform that the action execution is finished */
    public void actionExecuted(ActionExec act) {
        getTS().getC().addFeedbackAction(act);
        wakeUpAct();
    }

    /** Returns true if the agent can enter in sleep mode. */
    public boolean canSleep() {
        return (successor == null) || successor.canSleep();
    }

    /** Puts the agent in sleep. */
    /*public void sleep() {
        if (successor != null)
            successor.sleep();
    }*/

    public void wake() {
        if (successor != null)
            successor.wake();
    }

    public void wakeUpSense() {
        if (successor != null)
            successor.wakeUpSense();
    }

    public void wakeUpDeliberate() {
        if (successor != null)
            successor.wakeUpDeliberate();
    }

    public void wakeUpAct() {
        if (successor != null)
            successor.wakeUpAct();
    }

    /** return agent specific run time services (e.g. jade agents implements its differently for each agent) */
    public RuntimeServices getRuntimeServices() {
        if (successor == null)
            return null;
        else
            return successor.getRuntimeServices();
    }

    /** Gets the agent's name */
    public String getAgName() {
        if (successor == null)
            return "no-named";
        else
            return successor.getAgName();
    }

    /** Sends a Jason message */
    public void sendMsg(Message m) throws Exception {
        if (successor != null)
            successor.sendMsg(m);
    }

    /** Broadcasts a Jason message */
    public void broadcast(Message m) throws Exception {
        if (successor != null)
            successor.broadcast(m);
    }

    /** Checks whether the agent is running */
    public boolean isRunning() {
        return successor == null || successor.isRunning();
    }

    /** sets the number of the current cycle */
    public void setCycleNumber(int cycle) {
        cycleNumber = cycle;
        if (successor != null)
            successor.setCycleNumber(cycle);
    }

    public void incCycleNumber() {
        setCycleNumber(cycleNumber+1);
    }

    /** gets the current cycle number */
    public int getCycleNumber() {
        return cycleNumber;
    }

    @Override
    public String toString() {
        return "arch-"+getAgName();
    }

    @Override
    public int hashCode() {
        return getAgName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof AgArch arch) return this.getAgName().equals( arch.getAgName());
        return false;
    }

    public int compareTo(AgArch o) {
        return getAgName().compareTo(o.getAgName());
    }

    public Map<String,Object> getStatus() {
        if (successor != null)
            return successor.getStatus();
        else
            return new TreeMap<>();
    }
}
