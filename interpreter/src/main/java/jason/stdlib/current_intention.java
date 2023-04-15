package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.current_intention</code></b>.

  <p>Description: returns a description of the current intention. It is useful
  for plans that need to inspect the current intention. The description of the
  intention has the following form:<br><br>

  <code>intention(<i>id</i>,<i>stack of intended means</i>)</code><br><br>

  where each intended means has the form:<br><br>

  <code>im(<i>plan label</i>,<i>trigger event</i>,<i>plan body term</i>, <i>unification function (a list of maps)</i>)</code><br><br>

  For example:<br><br>

  <blockquote>
  <code>intention(1,<br>
  [<br>
  im(l__6[source(self)], {+!g3},     { .current_intention(I); .print(end) }, [map(I, ....)]),<br>
  im(l__5[source(self)], {+!g5(10)}, { !g3; .fail }, []),<br>
  im(l__4[source(self)], {+!start},  { !g5(X); .print(endg4) }, [map(X, test)]),<br>
  ...<br>
  ])</code>
  </blockquote>

  <p>Parameters:<ul>

  <li>- intention (structure): the variable that unifies with the intention
  description.</li>

  </ul>

  <p>Example:<ul>

  <li> <code>.current_intention(X)</code>: <code>X</code> unifies with the
  description of the current intention (i.e. the intention that executed this
  internal action).</li>

  </ul>

  <p>Notes:<ul>

  <li>The internal action .intention(ID,State,Stack,current) can be used in place of .current_intention. </li>
  <li>In case this internal action is used in the <i>body</i> of a plan, the intention that
      are executing the plan is used.</li>
  <li>In case this internal action is used in the <i>context</i> of a plan, the intention that
      produced the event is used.</li>
  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.intention
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
@Manual(
        literal=".current_intention(description)",
        hint="returns a description of the current intention",
        argsHint= {
                "the variable that unifies with the intention description"
        },
        argsType= {
                "structure"
        },
        examples= {
                ".current_intention(X): X unifies with the description of the current intention"
        },
        seeAlso= {
                "jason.stdlib.intend",
                "jason.stdlib.desire",
                "jason.stdlib.intention",
                "jason.stdlib.drop_all_desires",
                "jason.stdlib.drop_all_events",
                "jason.stdlib.drop_all_intentions",
                "jason.stdlib.drop_event",
                "jason.stdlib.drop_intention",
                "jason.stdlib.drop_desire",
                "jason.stdlib.succeed_goal",
                "jason.stdlib.fail_goal",
                "jason.stdlib.resume",
                "jason.stdlib.suspend",
                "jason.stdlib.suspended"
        }
    )
@SuppressWarnings("serial")
public class current_intention extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        ts.getLogger().warning(".current_intention( intention(I,S) ) was replaced by .intention(I,_,S,current)");

        // try to get the intention from the "body"
        Intention i = ts.getC().getSelectedIntention();

        if (i == null) {
            // try to get the intention from the event
            Event evt = ts.getC().getSelectedEvent();
            if (evt != null)
                i = evt.getIntention();
        }
        if (i != null)
            return un.unifies(i.getAsTerm(), args[0]);
        else
            return false;
    }
}
