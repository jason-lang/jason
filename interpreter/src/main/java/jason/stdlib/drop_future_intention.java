package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.Circumstance;
import jason.asSemantics.Intention;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.Term;

/**
  <p>
  Internal action: <b><code>.drop_future_intention(<i>I</i>)</code></b>.

  <p>
  Description: removes intentions that will try to achieve goal <i>I</i> from
  the set of intentions of the agent (suspended intentions are also
  considered). No event is produced.

  The current implementation consider only intended means (already instantiated
  plans), in the future we can consider to look ahead for possible selected
  plans.

  <p>Parameters:<ul>
  <li>- goal (literal): the goal the intentions achieve.</li>
  </ul>

  <p>
  Example:
  <ul>

  <li><code>.drop_future_intention(go(1,3))</code>: removes an intention having
  a plan that will execute <code>+!go(1,3)</code>.

  </ul>

  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
  @see jason.stdlib.resume

 */
@Manual(
        literal=".drop_future_intention(goal)",
        hint="removes intentions that will try to achieve goal the referred goal",
        argsHint= {
                "the goal the intentions achieve"
        },
        argsType= {
                "literal"
        },
        examples= {
                "drop_future_intention(go(1,3)): removes an intention having a plan that will execute +!go(1,3)"
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
                "jason.stdlib.current_intention",
                "jason.stdlib.resume",
                "jason.stdlib.suspend",
                "jason.stdlib.suspended"
        }
    )
@SuppressWarnings("serial")
public class drop_future_intention extends drop_desire {

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral())
            throw JasonException.createWrongArgument(this, "first argument '" + args[0] + "' must be a literal");
    }

    @Override
    public boolean dropInt(Circumstance C, Literal goal, Unifier un) {
        Unifier bak = un.clone();
        boolean isCurrentInt = false;
        Iterator<Intention> iint = C.getAllIntentions();
        while (iint.hasNext()) { // for all intentions
            Intention i = iint.next();

            // if i has the goal in the top IM's plan....
            PlanBody pb = i.peek().getPlan().getBody();
            while (pb != null) {
                if (pb.getBodyType() == BodyType.achieve || pb.getBodyType() == BodyType.achieveNF) {
                    if (un.unifies(pb.getBodyTerm(), goal)) {
                        C.dropIntention(i);
                        isCurrentInt = isCurrentInt || i.equals(C.getSelectedIntention());
                        un = bak.clone();
                        break;
                    }
                }
                pb = pb.getBodyNext();
            }
        }
        return isCurrentInt;
    }
}
