package jason.asSemantics;

import jason.asSyntax.Trigger;

/** call-back interface to be notified about events on goals */
public interface GoalListener {

    public enum GoalStates { started, suspended, resumed, finished, failed } ;
    public enum FinishStates { achieved, unachieved, dropped } ;

    /** method called when a new goal is produced by operator ! */
    public default void goalStarted(Event goal) {};

    /** method called when a goal is (un)successfully finished */
    public default void goalFinished(Trigger goal, FinishStates result) {};

    /** method called when a goal is failed */
    public default void goalFailed(Trigger goal) {};

    /** method called when a goal is suspended (waiting action on the environment or due to internal actions like .wait and .suspend) */
    public default void goalSuspended(Trigger goal, String reason) {};

    /** called when a suspended goal is resumed */
    public default void goalResumed(Trigger goal) {};

}
