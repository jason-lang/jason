// Environment code for project act-sync.mas2j

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.TimeSteppedEnvironment;

import java.util.logging.Logger;

import screen.Counters;

public class EnvSync extends TimeSteppedEnvironment {

    private Logger logger = Logger.getLogger(EnvSync.class.getName());

    int actionCount = 0; // counts the number of actions executed

    public EnvSync() {
        // use queue policy when an agent tries more than one action in the same cycle,
        // in queue policy, the second action is postponed for the next cycle.
        setOverActionsPolicy(OverActionsPolicy.queue);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        actionCount++;
        Literal p = ASSyntax.createLiteral("executed_actions", ASSyntax.createNumber(actionCount));
        clearPercepts();
        addPercept(p);
        return true;
    }

    @Override
    protected void stepStarted(int step) {
        //Counters.get().setTitle("Environment in step "+step);
    }

    @Override
    protected void stepFinished(int step, long time, boolean timeout) {
        try {
            Thread.sleep(30);
        } catch (Exception e) {}
        if (timeout) {
            logger.info("Step "+getStep()+" finished in "+time+" miliseconds, timeout = "+timeout);
            Counters.get().setTitle("Step time"+time);
        }
    }
}
