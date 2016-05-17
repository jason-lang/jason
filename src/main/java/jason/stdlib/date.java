package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
  <p>Internal action: <b><code>.date(YY,MM,DD)</code></b>.
  
  <p>Description: gets the current date (year, month, and day of the
  month).

  <p>Parameters:<ul>
  
  <li>+/- year (number): the year.</li/>
  <li>+/- month (number): the month (1--12).</li>
  <li>+/- day (number): the day (1--31).</li>
  
  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.date(Y,M,D)</code>: unifies Y with the current year, M
  with the current month, and D with the current day.</li>

  <li> <code>.date(2006,12,30)</code>: succeeds if the action is run on
  30/Dec/2006 and fails otherwise.</li>

  </ul>

  @see jason.stdlib.time
 */
public class date extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new date();
        return singleton;
    }

    @Override public int getMinArgs() { return 3; }
    @Override public int getMaxArgs() { return 3; }

    /** date(YY,MM,DD) */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        Calendar now = new GregorianCalendar();
        return un.unifies(args[0], new NumberTermImpl(now.get(Calendar.YEAR))) &&
               un.unifies(args[1], new NumberTermImpl(now.get(Calendar.MONTH))) &&
               un.unifies(args[2], new NumberTermImpl(now.get(Calendar.DAY_OF_MONTH)));
    }
}
