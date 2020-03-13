package ia;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

@SuppressWarnings("serial")
public class snapshot extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String fname = ((StringTerm)args[0]).getString();
        
        ts.runAtBeginOfNextCycle(new Runnable() {
            @Override public void run() {
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fname))) {
                    out.writeObject(ts.getAg());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });        
        return true;        
    }
}
