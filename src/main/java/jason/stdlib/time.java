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

  <p>Internal action: <b><code>.time(HH,MM,SS,MS)</code></b>.

  <p>Description: gets the current time (hour, minute, seconds, and milliseconds).

  <p>Parameters:<ul>

  <li>+/- hours (number): the hours (0--23).</li>
  <li>+/- minutes (number): the minutes (0--59).</li>
  <li>+/- seconds (number): the seconds (0--59).</li>
  <li>+/- milliseconds (number): the milliseconds (0--999).</li>

  </ul>

  <p>Examples:<ul>

  <li> <code>.time(H,M,S)</code>: unifies H with the current hour, M
  with the current minutes, and S with the current seconds.</li>

  <li> <code>.time(H,M,S,MS)</code>: unifies H with the current hour, M
  with the current minutes, S with the current seconds, and MS with the current
  milliseconds.</li>

  <li> <code>.time(15,_,_)</code>: succeeds if it is now 3pm or a bit later
  but not yet 4pm.</li>

  </ul>

  @see jason.stdlib.date
  @see jason.stdlib.nano_time
  @see jason.functions.time function time
 */
@Manual(
        literal=".time(hour,minute,second,milliseconds)",
        hint="gets the current time (hour, minute, seconds, and milliseconds).",
        argsHint= {
                "the hours (from 0 to 23)",
                "the minutes (from 0 to 59)",
                "the seconds (from 0 to 59)",
                "the milliseconds (from 0 to 999)",
        },
        argsType= {
                "number",
                "number",
                "number",
                "number"
        },
        examples= {
                ".time(H,M,S): unifies H with the current hour, M with the current minutes, and S with the current seconds",
                ".time(H,M,S,MS): unifies H with the current hour, M with the current minutes, S with the current seconds, and MS with the current milliseconds",
                ".time(15,_,_): succeeds if it is now 3pm or a bit later but not yet 4pm"
        },
        seeAlso= {
                "jason.stdlib.date",
                "jason.functions.time"
        }
    )
@SuppressWarnings("serial")
public class time extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new time();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 3;
    }
    @Override public int getMaxArgs() {
        return 4;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Calendar now = new GregorianCalendar();
        boolean un_bool = un.unifies(args[0], new NumberTermImpl(now.get(Calendar.HOUR_OF_DAY))) &&
               un.unifies(args[1], new NumberTermImpl(now.get(Calendar.MINUTE))) &&
               un.unifies(args[2], new NumberTermImpl(now.get(Calendar.SECOND)));

        if(args.length == 4){
            un_bool = un_bool && un.unifies(args[3], new NumberTermImpl(now.get(Calendar.MILLISECOND)));
        }
        return  un_bool;

    }
}
