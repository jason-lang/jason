package jason.infra.centralised;

import java.util.concurrent.ExecutorService;

import jason.asSemantics.ActionExec;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Message;
import jason.infra.components.ActComponent;
import jason.infra.components.DeliberateComponent;
import jason.infra.components.SenseComponent;

public class CentralisedAgArchAsynchronous extends CentralisedAgArch implements Runnable {
    private SenseComponent senseComponent;
    private DeliberateComponent deliberateComponent;
    private ActComponent actComponent;

    private ExecutorService executorSense;
    private ExecutorService executorDeliberate;
    private ExecutorService executorAct;

    public Object objSense = new Object();
    public Object objDeliberate = new Object();
    public Object objAct = new Object();

    public CentralisedAgArchAsynchronous() {
        super();

        senseComponent = new SenseComponent(this);
        deliberateComponent = new DeliberateComponent(this);
        actComponent = new ActComponent(this);
    }

    public void wakeUpSense() {
        senseComponent.wakeUp();
    }

    public void wakeUpDeliberate() {
        deliberateComponent.wakeUp();
    }

    public void wakeUpAct() {
        actComponent.wakeUp();
    }

    public SenseComponent getSenseComponent() {
        return senseComponent;
    }

    public DeliberateComponent getDeliberateComponent() {
        return deliberateComponent;
    }

    public ActComponent getActComponent() {
        return actComponent;
    }

    public ExecutorService getExecutorSense() {
        return executorSense;
    }


    public ExecutorService getExecutorDeliberate() {
        return executorDeliberate;
    }

    public ExecutorService getExecutorAct() {
        return executorAct;
    }

    public void setExecutorAct(ExecutorService executorAct) {
        this.executorAct = executorAct;
    }

    public void setExecutorSense(ExecutorService executorSense) {
        this.executorSense = executorSense;
    }

    public void setExecutorDeliberate(ExecutorService executorDeliberate) {
        this.executorDeliberate = executorDeliberate;
    }

    public void setSenseComponent(SenseComponent senseComponent) {
        this.senseComponent = senseComponent;
    }

    public void addListenerToC(CircumstanceListener listener) {
        getTS().getC().addEventListener(listener);
    }

    public void receiveMsg(Message m) {
        synchronized (objSense) {
            super.receiveMsg(m);
        }
    }

    /** called the the environment when the action was executed */
    public void actionExecuted(ActionExec action) {
        synchronized (objAct) {
            super.actionExecuted(action);
        }
    }
}
