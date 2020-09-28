package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.GoalListener;
import jason.asSemantics.GoalListener.GoalStates;
import jason.asSemantics.IMCondition;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

/**
  <p>Internal action:
  <b><code>.succeed_goal(<i>G</i>)</code></b>.

  <p>Description: remove goals <i>G</i> from the agent circumstance as if a plan
  for such goal had successfully finished. <i>G</i>
  is a goal if there is a triggering event <code>+!G</code> in any plan within any
  intention; also note that intentions can be suspended hence appearing
  in E, PA, or PI as well.
  <br/>
  The meta-event <code>^!G[state(finished)]</code> is produced.

  <p>Example:<ul>

  <li> <code>.succeed_goal(go(X,3))</code>: stops any attempt to achieve goals such as
  <code>!go(1,3)</code> as if it had already been achieved.

  </ul>

  (Note: this internal action was introduced in a DALT 2006 paper, where it was called .dropGoal(G,true).)

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.fail_goal
  @see jason.stdlib.intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

 */
@Manual(
        literal=".succeed_goal(goal)",
        hint="remove goals from the agent circumstance as if a plan for such goal had successfully finished",
        argsHint= {
                "the goals to be removed"
        },
        argsType= {
                "literal"
        },
        examples= {
                ".succeed_goal(go(X,3)): stops any attempt to achieve goals such as !go(1,3) as if it had already been achieved"
        },
        seeAlso= {
                "jason.stdlib.intend",
                "jason.stdlib.desire",
                "jason.stdlib.drop_all_desires",
                "jason.stdlib.drop_all_events",
                "jason.stdlib.drop_all_intentions",
                "jason.stdlib.drop_intention",
                "jason.stdlib.drop_desire",
                "jason.stdlib.fail_goal",
                "jason.stdlib.intention",
                "jason.stdlib.suspend",
                "jason.stdlib.suspended",
                "jason.stdlib.resume"
        }
    )

public class succeed_goal extends DefaultInternalAction {

    private static Term resumeReason = new Atom("suceed_internal_action");

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length > 0 && !args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        drop(ts, (Literal)args[0], un);
        return true;
    }

    public void drop(TransitionSystem ts, Literal l, Unifier un) throws Exception {
        final Trigger g = new Trigger(TEOperator.add, TEType.achieve, l);
        drop(ts, new IMCondition() {
            public boolean test(Trigger t, Unifier u) {
                return u.unifies(g, t);
            }
            @Override
            public Trigger getTrigger() {
                return g;
            }
        }, un);
    }

    public void drop(TransitionSystem ts, IMCondition c, Unifier un) throws Exception {
        Circumstance C = ts.getC();
        Unifier bak = un.clone();

        // remove goal before in events, since the deletion from I can produce events for the goal

        // dropping G in Events
        Iterator<Event> ie = C.getEventsPlusAtomic();
        while (ie.hasNext()) {
            Event e = ie.next();
            // test in the intention
            Intention i = e.getIntention();
            int r = dropIntention(i, c, ts, un);
            if (r > 0) {
                C.removeEvent(e);
                if (r == 1) {
                    C.resumeIntention(i,resumeReason);
                }
                un = bak.clone();
            } else {
                // test in the event
                Trigger t = e.getTrigger();
                if (i != Intention.EmptyInt && !i.isFinished()) {
                    t = t.capply(i.peek().getUnif());
                }
                if (c.test(t, un)) {
                    dropInEvent(ts,e,i);
                    un = bak.clone();
                }
            }
        }

        // dropping G in Pending Events
        for (String ek: C.getPendingEvents().keySet()) {
            // test in the intention
            Event e = C.getPendingEvents().get(ek);
            Intention i = e.getIntention();
            int r = dropIntention(i, c, ts, un);
            if (r > 0) {
                C.removePendingEvent(ek);
                if (r == 1) {
                    C.resumeIntention(i,resumeReason);
                }
                un = bak.clone();
            } else {
                // test in the event
                Trigger t = e.getTrigger();
                if (i != Intention.EmptyInt && !i.isFinished()) { //i.size() > 0) {
                    t = t.capply(i.peek().getUnif());
                }
                if (c.test(t, un)) {
                    dropInEvent(ts,e,i);
                    un = bak.clone();
                }
            }
        }

        Iterator<Intention> itint = C.getRunningIntentionsPlusAtomic();
        while (itint.hasNext()) {
            Intention i = itint.next();
            if (dropIntention(i, c, ts, un) > 1) {
                C.dropRunningIntention(i);
                un = bak.clone();
            }
        }

        // dropping the current intention?
        dropIntention(C.getSelectedIntention(), c, ts, un);
        un = bak.clone();

        // dropping from Pending Actions
        for (ActionExec a: C.getPendingActions().values()) {
            Intention i = a.getIntention();
            int r = dropIntention(i, c, ts, un);
            if (r > 0) { // i was changed
                C.removePendingAction(i.getId());       // remove i from PA
                if (r == 1) {                           // i must continue running
                    C.resumeIntention(i,resumeReason);  // and put the intention back in I
                }                                       // if r > 1, the event was generated and i will be back soon
                un = bak.clone();
            }
        }

        // dropping from Pending Intentions
        for (Intention i: C.getPendingIntentions().values()) {
            int r = dropIntention(i, c, ts, un);
            if (r > 0) {
                C.removePendingIntention(i.getId());
                if (r == 1) {
                    C.resumeIntention(i,resumeReason);
                }
                un = bak.clone();
            }
        }
    }

    /* returns: >0 the intention was changed
     *           1 = intention must continue running
     *           2 = fail event was generated and added in C.E
     *           3 = simply removed without event
     */
    public int dropIntention(Intention i, IMCondition c, TransitionSystem ts, Unifier un) throws JasonException {
        if (i != null) {
            IntendedMeans im = i.dropGoal(c,un);
            if (im != null) {
                //ts.getLogger().info("*** dropped "+im+"\nfrom "+i);
                if (i.getGIntention() != null) // it is a sibling intention from a g-plan (new JasonER), TODO: test if im is below e-plan
                    return 3;
                if (ts.hasGoalListener())
                    for (GoalListener gl: ts.getGoalListeners())
                        gl.goalFinished(im.getTrigger(), GoalStates.achieved);

                // continue the intention
                if (!i.isFinished()) { // could be finished after i.dropGoal() !!
                    if (ts.getC().getSelectedIntention() != i) // if i is not the current intention, remove
                        i.peek().removeCurrentStep();
                    ts.applyClrInt(i);
                    return 1;
                } else {
                    ts.applyClrInt(i);
                    return 3;
                }
            }
        }
        return 0;
    }

    void dropInEvent(TransitionSystem ts, Event e, Intention i) throws Exception {
        Circumstance C = ts.getC();
        C.removeEvent(e);
        if (ts.hasGoalListener())
            for (GoalListener gl: ts.getGoalListeners())
                gl.goalFinished(e.getTrigger(), GoalStates.achieved);
        if (i != null) {
            i.peek().removeCurrentStep();
            ts.applyClrInt(i);
            C.addRunningIntention(i);
        }
    }
}
