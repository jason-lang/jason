package jason.infra.repl;

import java.util.logging.Level;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

public class verbose extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (args.length>0 && args[0].isNumeric()) {
            NumberTerm n = (NumberTerm)args[0];
            switch ((int)n.solve()) {
            case 0: ts.getLogger().setLevel(Level.SEVERE); break;
            case 1: ts.getLogger().setLevel(Level.INFO); break;
            case 2: ts.getLogger().setLevel(Level.FINE); System.out.println("*****");break;
            }
            return true;
        } else {
            return false;
        }
    }
    
}
