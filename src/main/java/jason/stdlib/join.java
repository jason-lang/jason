package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.Term;
import jason.stdlib.fork.ForkData;

/** injected by .fork */
public class join extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new join();
        return singleton;
    }

    @Override public Term[] prepareArguments(Literal body, Unifier un) {
        return body.getTermsArray();
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
    }

    @Override public boolean suspendIntention()   {
        return true;
    }
    @Override public boolean canBeUsedInContext() {
        return false;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Intention currentInt = ts.getC().getSelectedIntention();
        ForkData fd = (ForkData) ((ObjectTerm)args[0]).getObject();
        fd.toFinish--;
        //System.out.println("** in join for "+currentInt.getId()+ " with "+fd);

        // in the case of fork and, all intentions should be finished to continue
        if (fd.isAnd) {
            if (fd.toFinish == 0) {
                //System.out.println("join finished!");
                currentInt.peek().removeCurrentStep();
                ts.getC().addIntention(currentInt);
            }
        } else {
            // the first intention has finished, drop others
            fd.intentions.remove(currentInt);
            for (Intention i: fd.intentions) {
                //System.out.println("drop "+i.getId());
                drop_intention.dropInt(ts.getC(), i);
            }
            currentInt.peek().removeCurrentStep();
            ts.getC().addIntention(currentInt);
        }

        return true;
    }

}
