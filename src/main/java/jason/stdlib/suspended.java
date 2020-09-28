package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

import java.util.Map;

/**
  <p>Internal action: <b><code>.suspended(<i>G</i>, <i>R</i>)</code></b>.

  <p>Description: checks whether goal <i>G</i> belongs to a suspended intention. <i>R</i> (a term)
  unifies with the reason for the
  suspend (waiting action to be performed, .wait, ....).

  The literal <i>G</i>
  represents a suspended goal if there is a triggering event <code>+!G</code> in any plan within
  any intention in PI or PA.

  <p>Example:<ul>

  <li> <code>.suspended(go(1,3),R)</code>: true if <code>go(1,3)</code>
  is a suspended goal. <code>R</code> unifies with "act" if the reason for being suspended
  is an action waiting feedback from environment.

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
  @see jason.stdlib.intention
  @see jason.stdlib.suspend
  @see jason.stdlib.resume

*/
@Manual(
        literal=".suspended(goal,reason)",
        hint="checks whether given goal belongs to a suspended intention. Unifies with the reason for the suspend (waiting action to be performed, .wait, ....)",
        argsHint= {
                "the goals to check if are suspended",
                "the reason for being suspended"
        },
        argsType= {
                "literal",
                "term"
        },
        examples= {
                ".suspended(go(1,3),R): true if go(1,3) is a suspended goal. R unifies with \"act\" if the reason is an action waiting feedback from environment"
        },
        seeAlso= {
                "jason.stdlib.intend",
                "jason.stdlib.desire",
                "jason.stdlib.drop_all_desires",
                "jason.stdlib.drop_all_events",
                "jason.stdlib.drop_all_intentions",
                "jason.stdlib.drop_intention",
                "jason.stdlib.drop_desire",
                "jason.stdlib.succeed_goal",
                "jason.stdlib.fail_goal",
                "jason.stdlib.intention",
                "jason.stdlib.suspend",
                "jason.stdlib.resume"
        }
    )
@SuppressWarnings("serial")
public class suspended extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }

    private static final Term aAct = ASSyntax.createAtom("act");

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Circumstance C = ts.getC();
        Trigger teGoal = new Trigger(TEOperator.add, TEType.achieve, (Literal)args[0]);

        // search in PA
        for (ActionExec a: C.getPendingActions().values())
            if (a.getIntention().hasTrigger(teGoal, un))
                return un.unifies(args[1], aAct);

        // search in PI
        Map<String, Intention> pi = C.getPendingIntentions();
        for (String id: pi.keySet()) {
            Intention i = pi.get(id);
            if (i.hasTrigger(teGoal, un)) {
                Term reason = i.getSuspendedReason();
                if (reason == null)
                    reason = new StringTermImpl(id);
                return un.unifies(args[1], reason);
            }
        }

        return false;
    }
}
