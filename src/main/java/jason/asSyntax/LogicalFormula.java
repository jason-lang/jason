package jason.asSyntax;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;

import java.util.Iterator;

/**
 * Represents a logical formula (p, p & q, not p, 3 > X, ...) which can be 
 * evaluated into a truth value.
 * 
 * @opt nodefillcolor lightgoldenrodyellow
 * 
 * @author Jomi
 */
public interface LogicalFormula extends Term, Cloneable {
    /**
     * Checks whether the formula is a
     * logical consequence of the belief base.
     * 
     * Returns an iterator for all unifiers that are consequence.
     */
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un);

}
