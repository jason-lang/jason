package jason.infra.repl;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class clear extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        ReplAg ag = (ReplAg)ts.getAg();
        ag.clear();
        return true;
    }
    
}
