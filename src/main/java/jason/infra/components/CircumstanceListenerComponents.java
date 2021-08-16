package jason.infra.components;

import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.infra.local.LocalAgArchAsynchronous;

public class CircumstanceListenerComponents implements CircumstanceListener {
    private LocalAgArchAsynchronous ag;

    public CircumstanceListenerComponents(LocalAgArchAsynchronous ag) {
        this.ag = ag;
    }

    public void notifyDeliberate() {
        ag.wakeUpDeliberate();
    }

    public void notifyAct() {
        ag.wakeUpAct();
    }

    @Override public void eventAdded(Event e) {
        notifyDeliberate();
    }

    @Override public void intentionAdded(Intention i) {
        notifyAct();
    }

    @Override public void intentionDropped(Intention i) {
        notifyDeliberate();
    }

    @Override public void intentionSuspended(Trigger t, Intention i, Term reason) {
        notifyDeliberate();
    }

    @Override public void intentionWaiting(Intention i, Term reason) {
        notifyDeliberate();
    }

    @Override public void intentionResumed(Intention i, Term reason) {
        notifyAct();
    }

    @Override
    public void intentionExecuting(Intention i, Term reason) {
        notifyAct();
    }
}
