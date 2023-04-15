package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
 * Common interface for all internal actions.
 *
 * @author Jomi
 */
public interface InternalAction {

    /** Returns true if the internal action (IA) should suspend the
        intention where the IA is called */
    boolean suspendIntention();

    /** Return true if the internal action can be used in plans' context */
    boolean canBeUsedInContext();

    /** Prepare body's terms to be used in 'execute', normally it consist of cloning and applying each term */
    public Term[] prepareArguments(Literal body, Unifier un);

    /** Executes the internal action. It should return a Boolean or
     *  an Iterator<Unifier>. A true boolean return means that the IA was
     *  successfully executed. An Iterator result means that there is
     *  more than one answer for this IA (e.g. see member internal action). */
    Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception;

    public void destroy() throws Exception;
}
