package jason.stdlib;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
  <p>Internal action: <b><code>.at</code></b>.

  <p>Description: creates an event at some time in the future. This command is
  based on the unix "at" command, although not fully implemented yet.

  <p>Parameters:<ul>
  <li>+ when (string): the time for the event to be generated.<br/>

      The syntax of this string in the current implementation has
      the following format:<br>
      <blockquote>
        <code>now + &lt;number> [&lt;time_unit>]</code>
      </blockquote > where &lt;time_unit> can be
      "s" or "second(s)",  "m" or "minute(s)", "h" or "hour(s)",
      "d" or "day(s)".
      The default &lt;time_unit> is milliseconds.<br/><br/>

  <li>+ event (trigger term): the event to be created. The event should
      follow the Jason Syntax for event and be
      enclosed by { and }.
      </ul>

  <p>Examples:<ul>
  <li> <code>.at("now +3 minutes", {+!g})</code>: generates the event <code>+!g</code> 3 minutes from now.
  <li> <code>.at("now +1 m", {+!g})</code>
  <li> <code>.at("now +2 h", {+!g})</code>
  </ul>

  @see jason.stdlib.wait

 */
public class at extends DefaultInternalAction {

    public static final String atAtom = ".at";

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        StringTerm time   = (StringTerm)args[0];
        String     stime  = time.getString();

        // parse time
        long deadline = -1;

        // if it starts with now
        if (stime.startsWith("now")) {
            // it is something like "now +3 minutes"
            stime = stime.substring(3).trim();
            // get the amount of time
            if (stime.startsWith("+")) {
                stime = stime.substring(1).trim();
                int pos = stime.indexOf(" ");
                if (pos > 0) {
                    deadline = Integer.parseInt(stime.substring(0,pos));
                    // get the time unit
                    stime = stime.substring(pos).trim();
                    if (stime.equals("s") || stime.startsWith("second")) {
                        deadline *= 1000;
                    }
                    if (stime.equals("m") || stime.startsWith("minute")) {
                        deadline *= 1000 * 60;
                    }
                    if (stime.equals("h") || stime.startsWith("hour")) {
                        deadline *= 1000 * 60 * 60;
                    }
                    if (stime.equals("d") || stime.startsWith("day")) {
                        deadline *= 1000 * 60 * 60 * 24;
                    }
                }
            }

        } else {
            throw new JasonException("The time parameter ('"+stime+"') of the internal action 'at' is not implemented!");
        }

        if (deadline == -1) {
            throw new JasonException("The time parameter ('"+time+"') of the internal action 'at' did not parse correctly!");
        }

        Trigger te = Trigger.tryToGetTrigger(args[1]);

        Agent.getScheduler().schedule(new CheckDeadline(te, ts), deadline, TimeUnit.MILLISECONDS);
        return true;
    }

    private static AtomicInteger idCount = new AtomicInteger(0);
    private Map<Integer,CheckDeadline> ats = new ConcurrentHashMap<Integer,CheckDeadline>();

    public void cancelAts() {
        for (CheckDeadline t: ats.values())
            t.cancel();
    }

    class CheckDeadline implements Runnable {
        private int     id = 0;
        private Event   event;
        private TransitionSystem ts;
        private boolean cancelled = false;

        public CheckDeadline(Trigger te, TransitionSystem ts) {
            this.id = idCount.incrementAndGet();
            this.event = new Event(te, Intention.EmptyInt);
            this.ts = ts;
            ats.put(id, this);
        }

        void cancel() {
            cancelled = true;
        }

        public void run() {
            try {
                if (!cancelled) {
                    ts.getC().addEvent(event);
                    ts.getUserAgArch().wake();
                }
            } finally {
                ats.remove(id);
            }
        }
    }
}
