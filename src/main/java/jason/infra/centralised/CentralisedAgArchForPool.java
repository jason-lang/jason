package jason.infra.centralised;

import java.util.concurrent.ExecutorService;

/** an agent architecture for the infra based on thread pool */
public final class CentralisedAgArchForPool extends CentralisedAgArch {
    private volatile boolean isSleeping  = false;
    private ExecutorService executor;

    public void setExecutor(ExecutorService e) {
        executor = e;
    }

    @Override
    public void sleep() {
        isSleeping = true;
        /*Agent.getScheduler().schedule(new Runnable() {
            public void run() {
                wake();
            }
        }, MAX_SLEEP, TimeUnit.MILLISECONDS);*/
    }

    @Override
    public void wake() {
        synchronized (this) {
            if (isSleeping) {
                isSleeping = false;
                executor.execute(this);
            }
        }
    }

    @Override
    public void run() {
        int number_cycles = getCycles();
        int i = 0;

        while (isRunning() && i++ < number_cycles) {
            reasoningCycle();
            synchronized (this) {
                if (getTS().canSleep()) {
                    sleep();
                    return;
                } else if (i == number_cycles) {
                    executor.execute(this);
                    return;
                }
            }
        }
    }
}

