package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

public class exit_error extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 0;
    }

    @Override public int getMaxArgs() {
        return 0;
    }

    @Override
    public boolean canBeUsedInContext() {
        return false;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        System.err.println("Exiting with error!");
        System.exit(-1);
        return true;
    }
}
