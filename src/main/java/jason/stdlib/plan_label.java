package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.plan_label(<i>P</i>,<i>L</i>)</code></b>.

  <p>Description: unifies <i>P</i> with a <i>plan term</i> representing the plan
  labeled with the term <i>L</i> within the agent's plan library.

  <p>Parameters:<ul>

  <li>- plan (plan term): the term representing the plan, it is
  a plan enclosed by { and }
  (e.g. <code>{+!g : vl(X) <- .print(X)}</code>).<br/>

  <li>+ label (structure): the label of that plan.<br/>

  </ul>

  <p>Example:<ul>

  <li> <code>.plan_label(P,p1)</code>: unifies P with the term
  representation of the plan labeled <code>p1</code>.</li>

  </ul>

  @see jason.stdlib.add_plan
  @see jason.stdlib.relevant_plans
  @see jason.stdlib.remove_plan

 */
public class plan_label extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new plan_label();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Term label = args[1];
        Plan p;
        if (label.isAtom())
            p = ts.getAg().getPL().get( (Atom)label);
        else
            p = ts.getAg().getPL().get( new Atom(label.toString()));

        if (p != null) {
            p = (Plan)p.clone();
            p.getLabel().delSources();
            p.setAsPlanTerm(true);
            p.makeVarsAnnon();
            //String ps = p.toASString().replaceAll("\"", "\\\\\"");
            //return un.unifies(new StringTermImpl(ps), args[0]);
            return un.unifies(p, args[0]);
        } else {
            return false;
        }
    }
}
