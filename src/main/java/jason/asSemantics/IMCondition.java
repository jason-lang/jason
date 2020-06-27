package jason.asSemantics;

import jason.asSyntax.Trigger;

/**
 * Used to generalise dropIM (in Intention)
 *
 * @author jomi
 */
public abstract class IMCondition {
    public boolean test(IntendedMeans im, Unifier u) {
        return test(im.getTrigger(),u);
    }
    public abstract boolean test(Trigger t, Unifier u);
    public abstract Trigger getTrigger();
}
