package ia;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.infra.local.RunLocalMAS;

@SuppressWarnings("serial")
public class snapshot extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String fname;
        if (args.length == 1) {
            fname = ((StringTerm)args[0]).getString();
        } else {
            String agName = args[0].toString();
            if (args[0].isString())
                agName = ((StringTerm)args[0]).getString();
            ts = RunLocalMAS.getRunner().getAg(agName).getTS();
            fname = ((StringTerm)args[1]).getString();
        }
        final Agent ag = ts.getAg();
        ts.runAtBeginOfNextCycle(() -> {
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fname))) {
                    out.writeObject(ag);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
        return true;
    }
}
