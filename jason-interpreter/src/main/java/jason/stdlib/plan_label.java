package jason.stdlib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.plan_label(<i>P</i>,<i>L</i>)</code></b>.

  <p>Description: unifies <i>P</i> with a <i>plan term</i> representing the plan
  labeled with the term <i>L</i> within the agent's plan library.

  <p>Parameters:<ul>

  <li>+/- plan (plan term): the term representing the plan, it is
  a plan enclosed by { and }
  (e.g. <code>{+!g : vl(X) <- .print(X)}</code>).<br/>

  <li>+/- label (structure): the label of that plan.<br/>

  </ul>

  <p>Example:<ul>

  <li> <code>.plan_label(P,p1)</code>: unifies P with the term
  representation of the plan labeled <code>p1</code>.</li>

  </ul>

  @see jason.stdlib.add_plan
  @see jason.stdlib.relevant_plans
  @see jason.stdlib.relevant_plan
  @see jason.stdlib.remove_plan

 */
@Manual(
		literal=".plan_label(plan,label)",
		hint="unifies with a plan term representing the plan labeled with the given term within the agent's plan library",
		argsHint= {
				"the term representing the plan enclosed by { and }",
				"the label of that plan"
		},
		argsType= {
				"plan term",
				"structure"
		},
		examples= {
				".plan_label(P,p1): unifies P with the term representation of the plan labeled p1"
		},
		seeAlso= {
				"jason.stdlib.add_plan",
				"jason.stdlib.plan_label",
				"jason.stdlib.relevant_plans",
				"jason.stdlib.remove_plan"
		}
	)
@SuppressWarnings("serial")
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

        // get plan
        if (args[0].isVar() && !args[1].isVar()) {
            Term label = args[1];
            Plan plan;
	        if (label.isLiteral())
	            plan = ts.getAg().getPL().get( (Literal)label);
	        else
	            plan = ts.getAg().getPL().get( new Atom(label.toString()));

	        if (plan == null)
	        	return false;

            plan = (Plan)plan.clone();
            //p.getLabel().delSources();
            plan.setAsPlanTerm(true);
            plan.makeVarsAnnon();
            return un.unifies(plan, args[0]);
        }

        // get label
        if (args[1].isVar() && !args[0].isVar()) {
            return un.unifies(args[1], ((Plan)args[0]).getLabel());
        }

        // backtrack on all plans
        // make a copy of all plans to avoid concurrent modification
        List<Plan> plans = new ArrayList<>(ts.getAg().getPL().getPlans());
        return new Iterator<Unifier>() {
            Iterator<Plan> i = plans.iterator();
            Unifier c = null; // the current response (which is an unifier)

            { find(); }

            public boolean hasNext() {
                return c != null;
            }

            public Unifier next() {
                Unifier b = c;
                find(); // find next response
                return b;
            }

            void find() {
                while (i.hasNext()) {
                	Plan p = i.next();
                    c = un.clone();
                    if (c.unifiesNoUndo(args[1], p.getLabel())) {
                    	p = (Plan)p.clone();
                    	p.setAsPlanTerm(true);
                    	p.makeVarsAnnon();
	                    if (c.unifiesNoUndo(args[0], p))
	                        return;
                    }
                }
                c = null; // no member is found,
            }
        };
    }
}
