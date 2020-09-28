package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.asSyntax.NumberTermImpl;

/**
  <p>Internal action: <b><code>.nano_time</code></b>.

  <p>Description: return system time in nano seconds

  <p>Parameters:<ul>

  <li>+ nano_time (number): system time in nano seconds.<br/>

  </ul>

  <p>Examples:<ul>

  <li> <code>.nano_time(T)</code>: unify in T the current system time in nano seconds.</li>

  </ul>

  @see jason.stdlib.date
  @see jason.stdlib.time
  @see jason.functions.time function time
*/
@Manual(
        literal=".nano_time(T)",
        hint="eturn system time in nano seconds",
        argsHint= {
                "system time in nano seconds"
        },
        argsType= {
                "number"
        },
        examples= {
                ".nano_time(T) unify in T the current system time in nano seconds"
        },
        seeAlso= {
                "jason.stdlib.create_agent",
                "jason.stdlib.save_agent"
        }
    )
@SuppressWarnings("serial")
public class nano_time extends DefaultInternalAction {

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
      checkArguments(args);

      return  un.unifies(args[0], new NumberTermImpl(System.nanoTime()));
    }
}
