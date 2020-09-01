package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;

/**
  <p>Internal action: <b><code>.remove_plan</code></b>.

  <p>Description: removes plans from the agent's plan library.

  <p>Parameters:<ul>

  <li>+ label(s) (atom or list of atoms or plan): the label of the
  plan to be removed. If this parameter is a list of labels, all plans
  of this list are removed.</li>

  <li><i>+ source</i> (atom [optional]): the source of the
  plan to be removed. The default value is <code>self</code>.</li>

  </ul>

  <p>Examples:<ul>

  <li> <code>.remove_plan(l1)</code>: removes the plan identified by
  label <code>l1[source(self)]</code>.</li>

  <li> <code>.remove_plan(l1,bob)</code>: removes the plan identified
  by label <code>l1[source(bob)]</code>. Note that a plan with a
  source like that was probably added to the plan library by a tellHow
  message.</li>

  <li> <code>.remove_plan([l1,l2,l3])</code>: removes the plans identified
  by labels <code>l1[source(self)]</code>, <code>l2[source(self)]</code>, and
  <code>l3[source(self)]</code>.</li>

  <li> <code>.remove_plan([l1,l2,l3],bob)</code>: removes the plans identified
  by labels <code>l1[source(bob)]</code>, <code>l2[source(bob)]</code>, and
  <code>l3[source(bob)]</code>.</li>

  <li> <code>.relevant_plans({ +!g }, _, LL); .remove_plan(LL)</code>:
  removes all plans with trigger event <code>+!g</code>.</li>

  <li>
  <code>for ( .plan_label( P, L[url("file:g.asl")]) ) {
        .remove_plan(P);
      }</code>: removes all achievement plans from source g.asl.
  </li>
  </ul>


  @see jason.stdlib.add_plan
  @see jason.stdlib.plan_label
  @see jason.stdlib.relevant_plans
  @see jason.stdlib.relevant_plan

 */
@Manual(
		literal=".remove_plan(labels[,source])",
		hint="removes plans from the agent's plan library",
		argsHint= {
				"the label of the plan to be removed",
				"the source of the plan to be removed (default value is self) [optional]"
		},
		argsType= {
				"structure or list of structures",
				"atom"
		},
		examples= {
				".remove_plan(l1): removes the plan identified by label l1[source(self)]",
				".remove_plan(l1,bob): removes the plan identified by label l1[source(bob)]. Note this plan was probably added by a tellHow message",
				".remove_plan([l1,l2,l3]): removes the plans identified by labels l1[source(self)], l2[source(self)], and l3[source(self)]",
				".remove_plan([l1,l2,l3],bob): removes the plans identified by labels l1[source(bob)], l2[source(bob)], and l3[source(bob)]",
				".relevant_plans({ +!g }, _, LL); .remove_plan(LL): removes all plans with trigger event +!g"
		},
		seeAlso= {
				"jason.stdlib.add_plan",
				"jason.stdlib.plan_label",
				"jason.stdlib.relevant_plans"
		}
	)
@SuppressWarnings("serial")
public class remove_plan extends DefaultInternalAction {

	@Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        Term label = args[0];
        if (args[0] instanceof Plan)
        	label = ((Plan)args[0]).getLabel();

        Term source = BeliefBase.ASelf;
        if (args.length > 1) {
            source = (Atom)args[1];
        }
        if (label.isList()) { // arg[0] is a list
            for (Term t: (ListTerm)args[0]) {
            	ts.getAg().getPL().remove(fixLabel(t), source);
            }
        } else { // args[0] is a plan label
        	ts.getAg().getPL().remove(fixLabel(label), source);
        }
        return true;
    }

    protected Literal fixLabel(Term label) throws ParseException {
    	if (label.isString() && ((StringTerm)label).getString().startsWith("@")) {
    		// as used in the book
    		label = ASSyntax.parseTerm(((StringTerm)label).getString().substring(1));
    	}
    	return (Literal)label;
    }
}
