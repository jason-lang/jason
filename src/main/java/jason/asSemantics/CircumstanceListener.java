package jason.asSemantics;


public interface CircumstanceListener {
    public default void eventAdded(Event e) {};
    public default void intentionAdded(Intention i) {};
    public default void intentionDropped(Intention i) {};
    public default void intentionSuspended(Intention i, String reason) {};
    public default void intentionResumed(Intention i) {};
}
