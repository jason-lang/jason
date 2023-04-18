package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.bb.BeliefBase;

/**
  <p>Internal action: <b><code>.signal</code></b>.

  <p>Description: adds an event into the event queue,
  e.g.: ".signal({+t})", ".signal({+!t})".

  <p>Parameter:<ul>
  <li>+ argument (an event)<br/>
  </ul>

*/
public class signal extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new signal();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        var te = Trigger.tryToGetTrigger(args[0]);
        if  (args.length == 2 && args[1].equals(new Atom("type_signal"))) {
            te.setType(Trigger.TEType.signal);
            te.getLiteral().addAnnot(new Atom("signal"));
        }
        if (!te.getLiteral().hasSource()) {
            te.getLiteral().addSource(BeliefBase.ASelf);
        }
        ts.updateEvents(
                new Event(
                        te,
                        Intention.EmptyInt));
        return true;
    }
}
