package jason.infra.repl;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

public class print_unifier extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        for (VarTerm v : un) {
            //if (! v.getFunctor().equals("Cmd__TR"))
                ts.getLogger().info(v+"="+un.get(v));            
        }
        ts.getLogger().info("done");
        return true;
    }
    
}
