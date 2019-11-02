package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

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
        return 1;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ts.updateEvents(
                new Event(
                        Trigger.tryToGetTrigger(args[0]),
                        Intention.EmptyInt));
        return true;
    }
}
