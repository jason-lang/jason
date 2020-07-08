package jason.asSemantics;

import java.io.Serializable;

import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/** call-back interface to be notified about events on goals */
public interface GoalListener extends Serializable {

    public enum GoalStates { started, suspended, resumed, achieved, dropped, failed, finished };

    /** method called when a new goal is produced by operator ! */
    public default void goalStarted(Event goal) {};

    /** method called when a goal is (un)successfully finished */
    public default void goalFinished(Trigger goal, GoalStates result) {};

    /** method called when a goal is failed */
    public default void goalFailed(Trigger goal) {};

    /** method called when a goal is suspended (waiting action on the environment or due to internal actions like .wait and .suspend) */
    public default void goalSuspended(Trigger goal, Term reason) {};

    /** called when a suspended goal is resumed */
    public default void goalResumed(Trigger goal, Term reason) {};

}
