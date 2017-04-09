package jason.infra.components;

import jason.infra.centralised.CentralisedAgArchAsynchronous;

public class ActComponent extends AgentComponent {

    public ActComponent(CentralisedAgArchAsynchronous centralisedAgArchAsynchronous) {
        super(centralisedAgArchAsynchronous);
    }

    public void wakeUp() {
        synchronized (ag.objAct) {
            if (sleeping) {
                sleeping = false;
                enqueueExecutor(false);
            }
        }
    }

    public void enqueueExecutor(boolean ts) {
        if (!inQueue || ts) {
            inQueue = true;
            ag.getExecutorAct().execute(this);
        }
    }

    public boolean canSleep() {
        return ag.getTS().canSleepAct();
    }

    public void run() {
        int cycles = ag.getCyclesAct();
        //int number_cycles = 1;
        int i = 0;
        while (ag.isRunning() && i < cycles) {
            i++;
            ag.getTS().act();


            synchronized (ag.objAct) {
                if (canSleep()) {
                    inQueue = false;
                    sleep();
                    return;
                } else if (i == cycles) {
                    enqueueExecutor(true);
                    return;
                }
            }
        }
    }
}
