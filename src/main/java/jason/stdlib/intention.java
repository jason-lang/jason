package jason.stdlib;

import java.util.Iterator;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.intention( ID, STATE, STACK) </code></b>.

  <p>Description: returns a description of an intention. It is useful
  for plans that need to inspect some intention. The description of each item of the
  intention stack (i.e., an intended means) has the following form:<br><br>

  <code>im(<i>plan label</i>,<i>trigger event</i>,<i>plan body term</i>, <i>unification function (a list of maps)</i>)</code><br><br>

  For example:<br><br>

  <blockquote>
  <code><br>
  [<br>
  im(l__6[source(self)], {+!g3},     { .current_intention(I); .print(end) }, [map(I, ....)]),<br>
  im(l__5[source(self)], {+!g5(10)}, { !g3; .fail }, []),<br>
  im(l__4[source(self)], {+!start},  { !g5(X); .print(endg4) }, [map(X, test)]),<br>
  ...<br>
  ]</code>
  </blockquote>

  <p>Parameters:<ul>

  <li>+/- intention id (number): the unique identifier of the intention. The special value of <code>current</code> can be used to get the intention executing this internal action. </li>
  <li>+/- intention state (atom): the state of the intention, suspended, running, ...</li>
  <li>-   intention stack (list, optional): all the intended means of the intention.</li>

  </ul>

  <p>Example:<ul>

  <li> <code>.intention(I,_)</code>: <code>I</code> unifies with all intention identifiers.</li>
  <li> <code>.intention(I,running)</code>: <code>I</code> unifies with identifiers of running intentions.</li>
  <li> <code>.intention(I,_,S)</code>: <code>S</code> unifies with intended means stack all intentions.</li>

  </ul>

  <p>Notes:<ul>

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
        return 3;
    }

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        return new Iterator<Unifier>() {
            Unifier solution = null; // the current response (which is an unifier)
            Intention curInt = null;
            Term argId = args[0];
            Iterator<Intention> intInterator = ts.getC().getAllIntentions();

            {
                if ("current".equals(args[0].toString()))
                    argId =  ASSyntax.createNumber( ts.getC().getSelectedIntention().getId() );
                find(); // find first answer
            }

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
                    curInt = intInterator.next();
                    solution = un.clone();
                    if (solution.unifiesNoUndo( argId,   ASSyntax.createNumber( curInt.getId())) &&
                        solution.unifiesNoUndo( args[1], ASSyntax.createAtom( curInt.getStateBasedOnPlace().toString())) ) {

                        if (args.length == 2)
                            return;
                        if (solution.unifiesNoUndo( args[2], curInt.getAsTerm().getTerm(1))) {
                            return;
                        }
                    }
                }
                solution = null; // nothing found
            }
        };
    }
}
