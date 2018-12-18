package jason.stdlib;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
  <p>Internal action: <b><code>.wait(<i>E</i>,<i>T</i>)</code></b>.

  <p>Description: suspend the intention for the time specified by <i>T</i> (in
  milliseconds) or until some event <i>E</i> happens. The events follow the
  AgentSpeak syntax but are enclosed by { and }, e.g. <code>{+bel(33)}</code>,
  <code>{+!go(X,Y)}</code>.

  <p>Parameters:<ul>
  <li><i>+ event</i> (trigger term [optional]): the event to wait for.<br/>
  <li><i>+ logical expression</i> ([optional]): the expression (as used on plans context) to wait to holds.<br/>
  <li>+ timeout (number [optional]): how many miliseconds should be waited.<br/>
  <li>- elapse time (var [optional]): the amount of time the intention was suspended waiting.<br/>
  </ul>


  <p>Examples:<ul>
  <li> <code>.wait(1000)</code>: suspend the intention for 1 second.

  <li> <code>.wait({+b(1)})</code>: suspend the intention until the belief
  <code>b(1)</code> is added in the belief base.

  <li> <code>.wait(b(X) & X > 10)</code>: suspend the intention until the agent believes
  <code>b(X)</code> with X greater than 10.

  <li> <code>.wait({+!g}, 2000)</code>: suspend the intention until the goal
  <code>g</code> is triggered or 2 seconds have passed, whatever happens
  first. In case the event does not happens in two seconds, the internal action
  fails.

  <li> <code>.wait({+!g}, 2000, EventTime)</code>: suspend the intention until the goal
  <code>g</code> is triggered or 2 seconds have passed, whatever happens
  first.
  As this use of .wait has three arguments, in case the event does not happen in
  two seconds, the internal action does not fail (as in the previous example).
  The third argument will be unified to the
  elapsed time (in miliseconds) from the start of .wait until the event or timeout. </ul>

  @see jason.stdlib.at

 */
public class wait extends DefaultInternalAction {

    public static final String waitAtom = ".wait";

    @Override public boolean canBeUsedInContext() {
        return false;
    }
    @Override public boolean suspendIntention()   {
        return true;
    }

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        long timeout = -1;
        Trigger te = null;
        LogicalFormula f = null;
        Term elapsedTime = null;

        if (args[0].isNumeric()) {
            // time in milliseconds
            NumberTerm time = (NumberTerm)args[0];
            timeout = (long) time.solve();
        } else {
            te = Trigger.tryToGetTrigger(args[0]);   // wait for event
            if (te == null && args[0] instanceof LogicalFormula) { // wait for an expression to become true
                f = (LogicalFormula)args[0];
                if (ts.getAg().believes(f, un)) { // if the agent already believes f
                    // place current intention back in I, since .wait usually does not do that
                    Intention si = ts.getC().getSelectedIntention();
                    si.peek().removeCurrentStep();
                    ts.getC().addIntention(si);
                    return true;
                }
            }
            if (args.length >= 2)
                timeout = (long) ((NumberTerm) args[1]).solve();
            if (args.length == 3)
                elapsedTime = args[2];
        }
        new WaitEvent(te, f, un, ts, timeout, elapsedTime);
        return true;
    }

    class WaitEvent implements CircumstanceListener {
        private Trigger          te;
        private LogicalFormula   formula;
        private String           sEvt; // a string version of what is being waited
        private Unifier          un;
        private Intention        si;
        private TransitionSystem ts;
        private Circumstance     c;
        private boolean          dropped = false;
        private Term             elapsedTimeTerm;
        private long             startTime;

        WaitEvent(Trigger te, LogicalFormula f, Unifier un, TransitionSystem ts, long timeout, Term elapsedTimeTerm) {
            this.te = te;
            this.formula = f;
            this.un = un;
            this.ts = ts;
            c = ts.getC();
            si = c.getSelectedIntention();
            this.elapsedTimeTerm = elapsedTimeTerm;

            // register listener
            c.addEventListener(this);

            if (te != null) {
                sEvt = te.toString();
            } else if (formula != null) {
                sEvt = formula.toString();
            } else {
                sEvt = "time"+(timeout);
            }
            sEvt = si.getId()+"/"+sEvt;
            c.addPendingIntention(sEvt, si);

            startTime = System.currentTimeMillis();

            if (timeout >= 0) {
                Agent.getScheduler().schedule(new Runnable() {
                    public void run() {
                        resume(true);
                    }
                }, timeout, TimeUnit.MILLISECONDS);
            }
        }

        void resume(final boolean stopByTimeout) {
            // unregister (to not receive intentionAdded again)
            c.removeEventListener(this);

            // invoke changes in C latter, so to avoid concurrent changes in C
            ts.runAtBeginOfNextCycle(new Runnable() {
                public void run() {
                    try {
                        // add SI again in C.I if (1) it was not removed (2) is is not running (by some other reason) -- but this test does not apply to atomic intentions --, and (3) this wait was not dropped
                        if (c.removePendingIntention(sEvt) == si && (si.isAtomic() || !c.hasIntention(si)) && !dropped) {
                            if (stopByTimeout && te != null && elapsedTimeTerm == null) {
                                // fail the .wait by timeout
                                if (si.isSuspended()) { // if the intention was suspended by .suspend
                                    PlanBody body = si.peek().getPlan().getBody();
                                    body.add(1, new PlanBodyImpl(BodyType.internalAction, new InternalActionLiteral(".fail")));
                                    c.addPendingIntention(suspend.SUSPENDED_INT+si.getId(), si);
                                } else {
                                    ts.generateGoalDeletion(si, JasonException.createBasicErrorAnnots("wait_timeout", "timeout in .wait"));
                                }
                            } else if (! si.isFinished()) {
                                si.peek().removeCurrentStep();

                                if (elapsedTimeTerm != null) {
                                    long elapsedTime = System.currentTimeMillis() - startTime;
                                    un.unifies(elapsedTimeTerm, new NumberTermImpl(elapsedTime));
                                }
                                if (si.isSuspended()) { // if the intention was suspended by .suspend
                                    c.addPendingIntention(suspend.SUSPENDED_INT+si.getId(), si);
                                } else {
                                    c.resumeIntention(si);
                                }
                            }
                        }
                    } catch (Exception e) {
                        ts.getLogger().log(Level.SEVERE, "Error at .wait thread", e);
                    }
                }
            });
            ts.getUserAgArch().wakeUpDeliberate();
        }

        public void eventAdded(Event e) {
            if (dropped)
                return;
            if (te != null && un.unifies(te, e.getTrigger())) {
                resume(false);
            } else if (formula != null && ts.getAg().believes(formula, un)) { // each new event, just test the formula being waited
                resume(false);
            }
        }

        public void intentionDropped(Intention i) {
            if (i.equals(si)) {
                dropped = true;
                resume(false);
            }
        }

        public void intentionAdded(Intention i) { }
        public void intentionResumed(Intention i) { }
        public void intentionSuspended(Intention i, String reason) { }
        public String toString() {
            return sEvt;
        }
    }
}
