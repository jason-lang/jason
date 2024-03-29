package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.GoalListenerForMetaEvents;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;


/**
  <p>Internal action: <b><code>.add_plan</code></b>.

  <p>Description: adds plan(s) to the agent's plan library.

  <p>Parameters:<ul>

  <li>+ plan(s) (plan term, string, or list): the <i>plan term</i> follows the
  same syntax as AS plans, but are enclosed by { and }
  (e.g. <code>{+!g : vl(X) <- .print(X)}</code>).
  In case this parameter is a string, the code within the string has to follow the syntax of AS plans
  (e.g. <code>"+!g : vl(X) <- .print(X)."</code>).
  If it is a list, each plan term or string in the list will be parsed into an
  AgentSpeak plan and added to the plan library.<br/>

  <li><i>+ source</i> (atom -- optional): the source of the
  plan(s). The default value for the source is <code>self</code>.<br/>

  <li><i>+ position</i> (atom -- optional): if the value is "begin" the plan
  will be added in the begin of the plan library.
  The default value is <code>end</code>.<br/>

  </ul>

  Note that if only two parameter is informed, the second will be the source and not
  the position.

  <p>Examples:<ul>

  <li> <code>.add_plan({ +b : true &lt;- .print(b) })</code>: adds the plan
  <code>+b : true &lt;- .print(b).</code> to the agent's plan library
  with a plan label annotated with <code>source(self)</code>.</li>

  <li> <code>.add_plan("+b : true &lt;- .print(b).")</code>: adds the plan
  <code>+b : true &lt;- .print(b).</code> to the agent's plan library
  with a plan label annotated with <code>source(self)</code>.</li>

  <li> <code>.add_plan({ +b : true &lt;- .print(b) }, rafa)</code>: same as
  the previous example, but the source of the plan is agent
  "rafa".</li>

  <li> <code>.add_plan({ +b : true &lt;- .print(b) }, rafa, begin)</code>: same as
  the previous example, but the plan is added at the beginning of the plan library.</li>

  <li> <code>.add_plan([{+b &lt;- .print(b)}, {+b : bel &lt;-
  .print(bel)}], rafa)</code>: adds both plans with "rafa" as their
  source.</li>

  </ul>

  @see jason.stdlib.plan_label
  @see jason.stdlib.relevant_plans
  @see jason.stdlib.relevant_plan
  @see jason.stdlib.remove_plan

  @author Jomi
 */
@Manual(
		literal=".add_plan(plan[,source,position])",
		hint="adds plan(s) to the agent's plan library",
		argsHint= {
				"the plan term enclosed by { and }",
				"the source of the plan(s) [optional]",
				"the position (begin/end) the plan will be added [optional]"
		},
		argsType= {
				"plan term, string, or list",
				"atom",
				"atom"
		},
		examples= {
				".add_plan({ +b : true &lt;- .print(b) }): adds the plan +b : true &lt;- .print(b). to the agent's plan library with source(self)",
				".add_plan(\"+b : true &lt;- .print(b).\"): adds the plan +b : true &lt;- .print(b). to the agent's plan library with source(self)",
				".add_plan({ +b : true &lt;- .print(b) }, rafa): same, but with source(rafa)",
				".add_plan({ +b : true &lt;- .print(b) }, rafa, begin): same, but the plan is added at the beginning of the plan library",
				".add_plan([{+b &lt;- .print(b)}, {+b : bel &lt;- .print(bel)}], rafa): adds both plans with source \"rafa\" and \"self\""
		},
		seeAlso= {
				"jason.stdlib.plan_label",
				"jason.stdlib.relevant_plans",
				"jason.stdlib.remove_plan"
		}
	)
@SuppressWarnings("serial")
public class add_plan extends DefaultInternalAction {

	@Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Term source = BeliefBase.ASelf;
        if (args.length > 1)
            source = args[1];

        boolean before = false;
        if (args.length > 2)
            before = args[2].toString().equals("begin");

        if (args[0].isList()) { // arg[0] is a list of strings
            for (Term t: (ListTerm) args[0]) {
                ts.getAg().getPL().add( transform2plan(t), source, before);
            }
        } else { // args[0] is a plan
            ts.getAg().getPL().add( transform2plan(args[0]), source, before);
        }

        if (ts.getAg().getPL().hasMetaEventPlans())
            ts.addGoalListener(new GoalListenerForMetaEvents(ts));

        return true;
    }

    private Plan transform2plan(Term t) throws ParseException, JasonException {
        Plan p = null;
        if (t.isString()) {
            String sPlan = ((StringTerm)t).getString();
            // remove quotes \" -> "
            StringBuilder sTemp = new StringBuilder();
            for (int c=0; c <sPlan.length(); c++) {
                if (sPlan.charAt(c) != '\\') {
                    sTemp.append(sPlan.charAt(c));
                }
            }
            sPlan  = sTemp.toString();
            p = ASSyntax.parsePlan(sPlan);
        } else if (t instanceof Plan tp) {
            p = tp;
        } /*else if (t instanceof VarTerm && ((VarTerm)t).hasValue() && ((VarTerm)t).getValue() instanceof Plan) {
            p = (Plan)((VarTerm)t).getValue();
        } */else {
            throw JasonException.createWrongArgument(this, "The term '"+t+"' ("+t.getClass().getSimpleName()+") can not be used as a plan for .add_plan.");
        }
        if (p.getLabel() != null && p.getLabel().getFunctor().startsWith("l__")) {
        	// if the label is automatic label, remove it
        	p.delLabel();
        }
        return p;
    }
}
