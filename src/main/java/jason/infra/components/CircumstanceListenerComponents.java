package jason.infra.components;

import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.infra.centralised.CentralisedAgArchAsynchronous;

public class CircumstanceListenerComponents implements CircumstanceListener {
    private CentralisedAgArchAsynchronous ag;

    public CircumstanceListenerComponents(CentralisedAgArchAsynchronous ag) {
        this.ag = ag;
    }

    public void notifyDeliberate() {
        ag.wakeUpDeliberate();
    }

    public void notifyAct() {
        ag.wakeUpAct();
    }

    public void eventAdded(Event e) {
        notifyDeliberate();
    }

    public void intentionAdded(Intention i) {
        notifyAct();
    }

    public void intentionDropped(Intention i) {
        notifyDeliberate();
    }

    public void intentionSuspended(Intention i, String reason) {
        notifyDeliberate();
    }

    public void intentionResumed(Intention i) {
        //notifyDeliberate();
        notifyAct();
    }
}
