package jason.environment;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;


/**
 * General environment class that "synchronise" all agents actions.
 * It waits one action for each agent and, when all actions is received,
 * executes them.
 *
 * @author Jomi
 *
 */
public class TimeSteppedEnvironment extends Environment {

    private Logger logger = Logger.getLogger(TimeSteppedEnvironment.class.getName());

    /** Policy used when a second action is requested and the agent still has another action pending execution */
    public enum OverActionsPolicy {
        /** Queue the second action request for future execution */
        queue,

        /** Fail the second action */
        failSecond,

        /** Ignore the second action, it is considered as successfully executed */
        ignoreSecond
    };

    private int step = 0;   // step counter
    private int nbAgs = -1; // number of agents acting on the environment
    private Map<String,ActRequest> requests; // actions to be executed
    private Queue<ActRequest> overRequests; // second action tentative in the step
    private TimeOutThread timeoutThread = null;
    private long stepTimeout = 0;
    private int  sleep = 0; // pause time between cycles


    private OverActionsPolicy overActPol = OverActionsPolicy.failSecond;

    public TimeSteppedEnvironment() {
        super(2);
    }

    /**
     * Resets step counter and scheduled action requests to neutral state, optionally sets a timeout for waiting
     * on agent actions in a step.
     *
     * @param args either empty, or contains timeout in milliseconds at pos 0
     */
    @Override
    public void init(String[] args) {
        super.init(args);

        if (args.length > 0) {
            try {
                stepTimeout = Integer.parseInt(args[0]);
            } catch (Exception e) {
                logger.warning("The argument "+args[0]+" is not a valid number for step timeout");
            }
        }

        // reset everything
        requests = new HashMap<String,ActRequest>();
        overRequests = new LinkedList<ActRequest>();
        step = 0;
        if (timeoutThread == null) {
            if (stepTimeout > 0) {
                timeoutThread = new TimeOutThread(stepTimeout);
                timeoutThread.start();
            }
        } else {
            timeoutThread.allAgFinished();
        }
        stepStarted(step);
    }

    /** defines the time for a pause between cycles */
    public void setSleep(int s) {
        sleep = s;
    }

    public void setTimeout(int to) {
        stepTimeout = to;
        if (timeoutThread != null)
            timeoutThread.timeout = to;
    }


    @Override
    public void stop() {
        super.stop();
        if (timeoutThread != null) timeoutThread.interrupt();
    }


    /**
     *  Updates the number of agents using the environment, this default
     *  implementation, considers all agents in the MAS as actors in the
     *  environment.
     */
    protected void updateNumberOfAgents() {
        try {
            setNbAgs(getEnvironmentInfraTier().getRuntimeServices().getAgentsNames().size());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns the number of agents in the MAS (used to test the end of a cycle) */
    public int getNbAgs() {
        return nbAgs;
    }

    /** Set the number of agents */
    public void setNbAgs(int n) {
        nbAgs = n;
    }

    /** returns the current step counter */
    public int getStep() {
        return step;
    }

    /**
     * Sets the policy used for the second ask for an action while another action is not finished yet.
     * If set as queue, the second action is added in a queue for future execution
     * If set as failSecond, the second action fails.
     */
    public void setOverActionsPolicy(OverActionsPolicy p) {
        overActPol = p;
    }

    @Override
    public void scheduleAction(String agName, Structure action, Object infraData) {
        if (!isRunning()) return;

        //System.out.println("scheduling "+action+" for "+agName);
        ActRequest newRequest = new ActRequest(agName, action, requiredStepsForAction(agName, action), infraData);

        boolean startNew = false;

        synchronized (requests) { // lock access to requests
            if (nbAgs < 0) { // || timeoutThread == null) {
                // initialise dynamic information
                // (must be in sync part, so that more agents do not start the timeout thread)
                updateNumberOfAgents();
                /*if (stepTimeout > 0 && timeoutThread == null) {
                    timeoutThread = new TimeOutThread(stepTimeout);
                    timeoutThread.start();
                }*/
            }

            // if the agent already has an action scheduled, fail the first
            ActRequest inSchedule = requests.get(agName);
            if (inSchedule != null) {
                logger.fine("Agent " + agName + " scheduled the additional action '" + action.toString() + "' in an "
                        + "occupied time step. Policy: " + overActPol.name());
                if (overActPol == OverActionsPolicy.queue) {
                    overRequests.offer(newRequest);
                } else if (overActPol == OverActionsPolicy.failSecond) {
                    getEnvironmentInfraTier().actionExecuted(agName, action, false, infraData);
                } else if (overActPol == OverActionsPolicy.ignoreSecond) {
                    getEnvironmentInfraTier().actionExecuted(agName, action, true, infraData);
                }
            } else {
                // store the action request
                requests.put(agName, newRequest);

                // test if all agents have sent their actions
                if (testEndCycle(requests.keySet())) {
                    startNew = true;
                }
            }

            if (startNew) {
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {}
                }
            }
        }

        if (startNew) {
            if (timeoutThread != null)
                timeoutThread.allAgFinished();
            else
                startNewStep();
        }
    }

    public Structure getActionInSchedule(String agName) {
        ActRequest inSchedule = requests.get(agName);
        if (inSchedule != null) {
            return inSchedule.action;
        }
        return null;
    }

    /**
     * Returns true when a new cycle can start, it normally
     * holds when all agents are in the finishedAgs set.
     *
     * @param finishedAgs the set of agents' name that already finished the current cycle
     */
    protected boolean testEndCycle(Set<String> finishedAgs) {
        return finishedAgs.size() >= getNbAgs();
    }

    /** This method is called after the execution of the action and before to send 'continue' to the agents */
    protected void updateAgsPercept() {
    }

    private void startNewStep() {
        if (!isRunning()) return;

        synchronized (requests) {
            step++;

            //logger.info("#"+requests.size());
            //logger.info("#"+overRequests.size());

            try {

                // execute all scheduled actions
                for (ActRequest a: requests.values()) {
                    a.remainSteps--;
                    if (a.remainSteps == 0) {
                        // calls the user implementation of the action
                        a.success = executeAction(a.agName, a.action);
                    }
                }

                updateAgsPercept();

                // notify the agents about the result of the execution
                Iterator<ActRequest> i = requests.values().iterator();
                while (i.hasNext()) {
                    ActRequest a = i.next();
                    if (a.remainSteps == 0) {
                        getEnvironmentInfraTier().actionExecuted(a.agName, a.action, a.success, a.infraData);
                        i.remove();
                    }
                }

                // clear all requests
                //requests.clear();

                // add actions waiting in over requests into the requests
                Iterator<ActRequest> io = overRequests.iterator();
                while (io.hasNext()) {
                    ActRequest a = io.next();
                    if (requests.get(a.agName) == null) {
                        requests.put(a.agName, a);
                        io.remove();
                    }
                }

                // the over requests could complete the requests
                // so test end of step again
                if (nbAgs > 0 && testEndCycle(requests.keySet())) {
                    startNewStep();
                }

                getEnvironmentInfraTier().informAgsEnvironmentChanged();

                stepStarted(step);
            } catch (Exception ie) {
                if (isRunning() && !(ie instanceof InterruptedException)) {
                    logger.log(Level.WARNING, "act error!",ie);
                }
            }
        }
    }

    /** to be overridden by the user class */
    protected void stepStarted(int step) {
    }

    /** to be overridden by the user class */
    protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
    }

    protected int requiredStepsForAction(String agName, Structure action) {
        return 1;
    }

    /** stops perception while executing the step's actions */
    @Override
    public Collection<Literal> getPercepts(String agName) {
        synchronized (requests) {
            return super.getPercepts(agName);
        }
    }

    class ActRequest {
        String agName;
        Structure action;
        Object infraData;
        boolean success;
        int remainSteps; // the number os steps this action have to wait to be executed
        public ActRequest(String ag, Structure act, int rs, Object data) {
            agName = ag;
            action = act;
            infraData = data;
            remainSteps = rs;
        }
        public boolean equals(Object obj) {
            return agName.equals(obj);
        }
        public int hashCode() {
            return agName.hashCode();
        }
        public String toString() {
            return "["+agName+","+action+"]";
        }
    }

    class TimeOutThread extends Thread {
        Lock lock = new ReentrantLock();
        Condition agActCond = lock.newCondition();
        long timeout = 0;
        boolean allFinished = false;

        public TimeOutThread(long to) {
            super("EnvironmentTimeOutThread");
            timeout = to;
        }

        public void allAgFinished() {
            lock.lock();
            allFinished = true;
            agActCond.signal();
            lock.unlock();
        }

        public void run() {
            try {
                while (true) {
                    lock.lock();
                    long lastStepStart = System.currentTimeMillis();
                    boolean byTimeOut = false;
                    if (!allFinished) {
                        byTimeOut = !agActCond.await(timeout, TimeUnit.MILLISECONDS);
                    }
                    allFinished = false;
                    long now  = System.currentTimeMillis();
                    long time = (now-lastStepStart);
                    stepFinished(step, time, byTimeOut);
                    lock.unlock();
                    startNewStep();
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error in timeout thread!",e);
            }
        }
    }
}
