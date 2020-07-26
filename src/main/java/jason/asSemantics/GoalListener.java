package jason.asSemantics;

import java.io.Serializable;

import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/** call-back interface to be notified about events on goals */
public interface GoalListener extends Serializable {

    public enum GoalStates { pending, executing, suspended, resumed, waiting, achieved, dropped, failed, finished };

    /** method called when a new goal is produced by operator ! */
    public default void goalStarted(Event goal) {};

    /** method called when a goal is (un)successfully finished */
    public default void goalFinished(Trigger goal, GoalStates result) {};

    /** method called when a goal is failed */
    public default void goalFailed(Trigger goal, Term reason) {};

    /** method called when a goal is suspended (by internal action .suspend, for instance) */
    public default void goalSuspended(Trigger goal, Term reason) {};

    /** method called when a goal is waiting something (waiting action on the environment or due to internal actions like .waitd) */
    public default void goalWaiting(Trigger goal, Term reason) {};

    /** called when a suspended goal is resumed */
    public default void goalResumed(Trigger goal, Term reason) {};

    /** called when a suspended goal is executing */
    public default void goalExecuting(Trigger goal, Term reason) {};

}
