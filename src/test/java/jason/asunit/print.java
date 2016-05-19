package jason.asunit;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.stdlib.println;

public class print extends println {
    
    public static InternalAction create() {
        return new print();
    }
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)  throws Exception {
        TestArch arch = (TestArch)ts.getUserAgArch();
        arch.print(argsToString(args));
        return true;
    }
}
