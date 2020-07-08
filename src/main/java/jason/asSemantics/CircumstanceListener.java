package jason.asSemantics;

import java.io.Serializable;

import jason.asSyntax.Term;

public interface CircumstanceListener extends Serializable {
    public default void eventAdded(Event e) {};
    public default void intentionAdded(Intention i) {};
    public default void intentionDropped(Intention i) {};
    public default void intentionSuspended(Intention i, Term reason) {};
    public default void intentionResumed(Intention i, Term reason) {};
}
