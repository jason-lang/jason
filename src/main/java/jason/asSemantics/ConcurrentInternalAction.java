package jason.asSemantics;

import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**

  This class can be used in place of DefaultInternalAction to create an IA that
  suspend the intention while it is being executed.

  Example: a plan may ask something to an user and wait the answer.
  If DefaultInternalAction is used for that, all the agent thread is blocked until
  the answer. With ConcurrentInternalAction, only the intention using the IA is
  suspended. See demos/gui/gui1.

  The code of an internal action that extends this class looks like:

  <pre>
  public class ...... extends ConcurrentInternalAction {

    public Object execute(final TransitionSystem ts, Unifier un, final Term[] args) throws Exception {
        ....

        final String key = suspendInt(ts, "gui", 5000); // suspend the intention (max 5 seconds)

        startInternalAction(ts, new Runnable() { // to not block the agent thread, start a thread that performs the task and resume the intention latter
            public void run() {

                .... the code of the IA .....

                if ( ... all Ok ...)
                    resumeInt(ts, key); // resume the intention with success
                else
                    failInt(ts, key); // resume the intention with fail
            }
        });

        ...
    }

    public void timeout(TransitionSystem ts, String intentionKey) { // called back when the intention should be resumed/failed by timeout (after 5 seconds in this example)
        ... this method have to decide what to do with actions finished by timeout: resume or fail
        ... to call resumeInt(ts,intentionKey) or failInt(ts, intentionKey)
    }
  }
  </pre>

  @author jomi
*/
public abstract class ConcurrentInternalAction implements InternalAction {

    private static AtomicInteger actcount  = new AtomicInteger(0);

    public boolean canBeUsedInContext() {
        return false;
    }

    public boolean suspendIntention() {
        return true;
    }

    public Term[] prepareArguments(Literal body, Unifier un) {
        Term[] terms = new Term[body.getArity()];
        for (int i=0; i<terms.length; i++) {
            terms[i] = body.getTerm(i).capply(un);
        }
        return terms;
    }

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return false;
    }

    /**
     * Suspend the current intention, put it in the PendingIntention (PI) structure and assigns it to a key.
     *
     * @param ts        the "engine" of the agent
     * @param basekey   the base key to form final key used to get the intention back from PI (e.g. "moise", "cartago", ...)
     * @param timeout   the max time the intention will be in PI, the value 0 means until "resume"
     * @return the final key used to store the intention in PI, this key is used the resume the intention
     */
    public String suspendInt(final TransitionSystem ts, String basekey, int timeout) {
        final String key = basekey + "/" + (actcount.incrementAndGet());
        final Circumstance C = ts.getC();
        Intention i = C.getSelectedIntention();
        i.setSuspended(true);
        C.addPendingIntention(key, i);

        if (timeout > 0) {
            // schedule a future test of the end of the action
            Agent.getScheduler().schedule( new Runnable() {
                public void run() {
                    // finish the IA by timeout
                    if (C.getPendingIntentions().get(key) != null) { // test if the intention is still there
                        timeout(ts,key);
                    }
                }
            }, timeout, TimeUnit.MILLISECONDS);
        }
        return key;
    }

    public void startInternalAction(TransitionSystem ts, Runnable code) {
        Agent.getScheduler().execute(code);
        //new Thread(code).start();
    }

    /** called back when some intention should be resumed/failed by timeout */
    abstract public void timeout(TransitionSystem ts, String intentionKey);

    /** resume the intention identified by intentionKey */
    public void resumeInt(TransitionSystem ts, String intentionKey) {
        resume(ts, intentionKey, false, null);
    }

    /** fails the intention identified by intentionKey */
    public void failInt(TransitionSystem ts, String intentionKey) {
        resume(ts, intentionKey, true, JasonException.createBasicErrorAnnots( "fail_resume", "Error resuming pending intention"));
    }

    synchronized public static void resume(final TransitionSystem ts, final String intentionKey, final boolean abort, final List<Term> failAnnots) {
        // invoke changes in C latter, so to avoid concurrent changes in C
        ts.runAtBeginOfNextCycle(new Runnable() {
            public void run() {
                Circumstance C = ts.getC();
                Intention pi = C.removePendingIntention(intentionKey);
                if (pi != null) {
                    pi.setSuspended(false);
                    try {
                        if (abort) {
                            // fail the IA
                            ts.generateGoalDeletion(pi, failAnnots);
                        } else {
                            pi.peek().removeCurrentStep(); // remove the internal action that put the intention in suspend
                            ts.applyClrInt(pi);
                            C.resumeIntention(pi); // add it back in I
                        }
                    } catch (JasonException e) {
                        ts.getLogger().log(Level.SEVERE, "Error resuming intention", e);
                    }
                }
            }
        });
        ts.getUserAgArch().wakeUpDeliberate();
    }

    public void destroy() throws Exception {
    }
}
