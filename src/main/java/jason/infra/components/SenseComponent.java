package jason.infra.components;

import jason.infra.centralised.CentralisedAgArchAsynchronous;

public class SenseComponent extends AgentComponent {

    public SenseComponent(CentralisedAgArchAsynchronous centralisedAgArchAsynchronous) {
        super(centralisedAgArchAsynchronous);
    }

    public boolean canSleep() {
        return ag.getTS().canSleepSense();
    }

    public void wakeUp() {
        synchronized (ag.objSense) {
            if (sleeping) {
                sleeping = false;
                enqueueExecutor(false);
            }
        }
    }

    /*
    public void wakeUp(boolean ts) {
        synchronized (ag.objSense) {
            if (ts || sleeping) {
                sleeping = false;
                enqueueExecutor(ts);
            }
        }
    }*/

    public void enqueueExecutor(boolean ts) {
        if (!inQueue || ts) {
            inQueue = true;
            ag.getExecutorSense().execute(this);
        }
    }

    public void run() {
        int cycles = ag.getCyclesSense();
        //int number_cycles = 1;
        int i = 0;

        while (ag.isRunning() && i < cycles) {
            i++;
            ag.getTS().sense();

            synchronized (ag.objSense) {
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
