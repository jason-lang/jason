package jason.stdlib;

import java.util.Iterator;

import jason.asSemantics.Circumstance.IntentionPlace;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Intention.State;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.intention( ID, STATE, STACK, [current]) </code></b>.

  <p>Description: returns a description of an intention. It is useful
  for plans that need to inspect some intention. The description of each item of the
  intention stack (i.e., an intended means) has the following form:<br><br>

  <code>im(<i>plan label</i>,<i>trigger event</i>,<i>plan body term</i>, <i>unification function (a list of maps)</i>)</code><br><br>

  For example:<br><br>

  <blockquote>
  <code><br>
  [<br>
  im(l__6[code_line(10),code_src("karlos.asl"),source(self)], {+!g3},     { .intention(I); .print(end) }, [map(I, ....)]),<br>
  im(l__5[code_line(15),code_src("karlos.asl"),source(self)], {+!g5(10)}, { !g3; .fail }                , []),<br>
  im(l__4[code_line(18),code_src("karlos.asl"),source(self)], {+!start},  { !g5(X); .print(endg4) }     , [map(X, test)]),<br>
  ...<br>
  ]</code>
  </blockquote>

  <p>Parameters:<ul>

  <li>+/- intention id (number): the unique identifier of the intention.
  <li>+/- intention state (atom): the state of the intention, suspended, running, ...</li>
  <li>-   intention stack (list, optional): all the intended means of the intention.</li>
  <li>+   current intention (atom, optional): selects only the current intention.</li>

  </ul>

  <p>Example:<ul>

  <li> <code>.intention(I,_)</code>: <code>I</code> unifies with all intention identifiers.</li>
  <li> <code>.intention(I,running)</code>: <code>I</code> unifies with identifiers of running intentions.</li>
  <li> <code>.intention(I,_,S)</code>: <code>S</code> unifies with intended means stack all intentions.</li>
  <li> <code>.intention(I,_,_,current)</code>: <code>I</code> unifies with the id of the current intention.</li>

  </ul>

  <p>Notes:<ul>

  <li>For possible states, see https://github.com/jason-lang/jason/blob/master/doc/tech/intention-states.pdf.
        Some states can be annotated with further details, for instance: waiting[reason(wait(5)[time(400)])]].
  </li>
  <li>In case this internal action is used in the <i>body</i> of a plan, the intention that
      are executing the plan is used as <code>current</code>.</li>
  <li>In case this internal action is used in the <i>context</i> of a plan, the intention that
      produced the event is used <code>current</code>.</li>
  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume
*/
public class intention extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 4;
    }

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);

        if (args.length == 4 && "current".equals(args[3].toString())) {
            Intention curInt = null; // current intention (executing this internal action)

            curInt = ts.getC().getSelectedIntention();
            if (curInt == null) {
                // try to get the intention from the current event
                Event evt = ts.getC().getSelectedEvent();
                if (evt != null)
                    curInt = evt.getIntention();
            }
            if (curInt == null) {
                ts.getLogger().warning(".intention wants the current intention, but I cannot identify which is the current intention!");
            } else {
                Unifier solution = un.clone();
                if (solution.unifiesNoUndo( args[0], ASSyntax.createNumber( curInt.getId())) &&
                    solution.unifiesNoUndo( args[1], ASSyntax.createAtom( State.running.toString())) &
                    solution.unifiesNoUndo( args[2], curInt.getAsTerm().getTerm(1))) {
                    un.compose(solution);
                    return true;
                }
            }
            return false;
        }

        return new Iterator<Unifier>() {
            Unifier solution = null; // the current response (which is an unifier)
            Intention actInt = null; // intention being considered
            Iterator<Intention> intInterator = ts.getC().getAllIntentions();

            { find(); } // find first answer

            public boolean hasNext() {
                return solution != null;
            }

            public Unifier next() {
                if (solution == null) find();
                Unifier b = solution;
                find(); // find next response
                return b;
            }

            void find() {
                while (intInterator.hasNext()) {
                    actInt = intInterator.next();

                    solution = un.clone();
                    if (solution.unifiesNoUndo( args[0], ASSyntax.createNumber( actInt.getId())) &&
                        solution.unifiesNoUndo( args[1], computeState(actInt) )) {

                        if (args.length == 2) {
                            return;
                        }
                        if (solution.unifiesNoUndo( args[2], actInt.getAsTerm().getTerm(1))) {
                            return;
                        }
                    }
                }
                solution = null; // nothing found
            }

            Term computeState(Intention i) {
                if (i.getPlace() == IntentionPlace.PendingActions) {
                    return ASSyntax.createLiteral(i.getStateBasedOnPlace().toString())
                            .addAnnots(ASSyntax.createLiteral("action",i.getSuspendedReason()));
                } else if (i.getStateBasedOnPlace() == State.waiting) {
                    return ASSyntax.createLiteral(i.getStateBasedOnPlace().toString())
                            .addAnnots(ASSyntax.createLiteral("reason",i.getSuspendedReason()));
                }
                return ASSyntax.createAtom(i.getStateBasedOnPlace().toString());
            }
        };
    }
}
