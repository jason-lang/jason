package jason.infra.components;

import jason.infra.local.LocalAgArchAsynchronous;

public abstract class AgentComponent implements Runnable {
    protected LocalAgArchAsynchronous ag;
    protected boolean inQueue = true;
    protected boolean sleeping = false;

    public AgentComponent(LocalAgArchAsynchronous ag) {
        this.ag = ag;
    }

    public void sleep() {
        sleeping = true;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public abstract void wakeUp();
    public abstract void enqueueExecutor(boolean ts);
    public abstract boolean canSleep();
}
