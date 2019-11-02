package jason.stdlib;

import java.util.Iterator;

import jason.asSemantics.Circumstance;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

/**
  <p>Internal action: <b><code>.desire(<i>D</i>)</code></b>.

  <p>Description: checks whether <i>D</i> is a desire: <i>D</i> is a desire
  either if there is an event with <code>+!D</code> as triggering
  event or it is a goal in one of the agent's intentions.

  <p>Parameters:<ul>

  <li>- desire (literal): the desire to be checked if it is present.</li>

  </ul>

  <p>Example:<ul>

  <li> <code>.desire(go(1,3))</code>: true if <code>go(1,3)</code>
  is a desire of the agent.

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

*/
@Manual(
		literal=".desire(desire)",
		hint="checks whether the argument is a desire",
		argsHint= {
				"the desire to be checked if it is present"
		},
		argsType= {
				"literal"
		},
		examples= {
				".desire(go(1,3)): true if go(1,3) is a desire of the agent"
		},
		seeAlso= {
				"jason.stdlib.intend",
				"jason.stdlib.drop_all_desires",
				"jason.stdlib.drop_all_events",
				"jason.stdlib.drop_all_intentions",
				"jason.stdlib.drop_event",
				"jason.stdlib.drop_intention",
				"jason.stdlib.drop_desire",
				"jason.stdlib.succeed_goal",
				"jason.stdlib.fail_goal",
				"jason.stdlib.current_intention",
				"jason.stdlib.resume",
				"jason.stdlib.suspend",
				"jason.stdlib.suspended"
		}
	)
@SuppressWarnings("serial")
public class desire extends intend {

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return allDesires(ts.getC(),(Literal)args[0],args.length == 2 ? args[1] : null, un);
    }

    enum Step { selEvt, evt, useIntends, end }

    public static Iterator<Unifier> allDesires(final Circumstance C, final Literal l, final Term intAsTerm, final Unifier un) {
        final Trigger teFromL = new Trigger(TEOperator.add, TEType.achieve, l);

        return new Iterator<Unifier>() {
            Step curStep = Step.selEvt;
            Unifier solution = null; // the current response (which is an unifier)
            Iterator<Event>      evtIterator     = null;
            Iterator<Unifier>    intendInterator = null;

            { find(); }

            public boolean hasNext() {
                return solution != null;
            }

            public Unifier next() {
                if (solution == null) find();
                Unifier b = solution;
                find(); // find next response
                //logger.info("* try "+b+" for "+teFromL);
                return b;
            }
            public void remove() {}

            void find() {
                switch (curStep) {

                case selEvt:
                    curStep = Step.evt; // set next step

                    // we need to check the selected event in this cycle (already removed from E)
                    if (C.getSelectedEvent() != null) {
                        Trigger   t = C.getSelectedEvent().getTrigger();
                        Intention i = C.getSelectedEvent().getIntention();
                        if (i != Intention.EmptyInt && !i.isFinished()) {
                            t = (Trigger)t.capply(i.peek().getUnif());
                        }
                        solution = un.clone();
                        if (solution.unifiesNoUndo(teFromL, t)) {
                            return;
                        }
                    }
                    find();
                    return;

                case evt:
                    if (evtIterator == null)
                        evtIterator = C.getEventsPlusAtomic();

                    if (evtIterator.hasNext()) {
                        Event ei = evtIterator.next();
                        Trigger   t = ei.getTrigger();
                        Intention i = ei.getIntention();
                        if (i != Intention.EmptyInt && !i.isFinished()) {
                            t = t.capply(i.peek().getUnif());
                        }
                        solution = un.clone();
                        if (solution.unifiesNoUndo(teFromL, t)) {
                            return;
                        }
                    } else {
                        curStep = Step.useIntends; // set next step
                    }
                    find();
                    return;

                case useIntends:
                    if (intendInterator == null)
                        intendInterator = allIntentions(C,l,intAsTerm,un);

                    if (intendInterator.hasNext()) {
                        solution = intendInterator.next();
                        return;
                    } else {
                        curStep = Step.end; // set next step
                    }

                case end:

                }
                solution = null; // nothing found
            }
        };
    }
}
