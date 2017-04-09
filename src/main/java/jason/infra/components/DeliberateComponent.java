package jason.infra.components;

import jason.infra.centralised.CentralisedAgArchAsynchronous;

public class DeliberateComponent extends AgentComponent {

    public DeliberateComponent(CentralisedAgArchAsynchronous centralisedAgArchAsynchronous) {
        super(centralisedAgArchAsynchronous);
    }

    public boolean canSleep() {
        return ag.getTS().canSleepDeliberate();
    }

    public void wakeUp() {
        synchronized (ag.objDeliberate) {
            if (sleeping) {
                sleeping = false;
                enqueueExecutor(false);
            }
        }
    }

    public void enqueueExecutor(boolean ts) {
        if (!inQueue || ts) {
            inQueue = true;
            ag.getExecutorDeliberate().execute(this);
        } else {
            System.out.println("It's already in the queue! DELIBERATE");
        }
    }

    public void run() {
        int cycles = ag.getCyclesDeliberate();
        //int number_cycles = 1;
        int i = 0;

        while (ag.isRunning() && i < cycles) {
            i++;

            ag.getTS().deliberate();

            synchronized (ag.objDeliberate) {

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
