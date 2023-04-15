package jason.stdlib;

import java.util.List;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.parser.ParseException;


/**
  <p>Internal action: <b><code>.relevant_plans</code></b>.

  <p>Description: gets all relevant plans for some triggering event. This
  internal action is used, for example, to answer "askHow" messages.

  <p>Parameters:<ul>

  <li>+ trigger (trigger): the triggering event, enclosed by { and }.</li>

  <li>- plans (list of plan terms): the list of plan terms corresponding to
  the code of the relevant plans.</li>

  <li><i>- labels</i> (list, optional): the list of labels of the plans.</li>

  </ul>

  <p>Example:<ul>

  <li> <code>.relevant_plans({+!go(X,Y)},LP)</code>: unifies LP with a list of
  all plans in the agent's plan library that are relevant for the triggering
  event <code>+!go(X,Y)</code>.</li>

  <li> <code>.relevant_plans({+!go(X,Y)},LP, LL)</code>: same as above but also
  unifies LL with a list of labels of plans in LP.</li>

  <li> <code>.relevant_plans({+!_},_,LL)</code>: gets the labels of all plans for achievement goals.</li>

  </ul>

  @see jason.stdlib.add_plan
  @see jason.stdlib.plan_label
  @see jason.stdlib.remove_plan
 */
@Manual(
		literal=".relevant_plans(trigger,plans[,labels])",
		hint="gets all relevant plans for some triggering event",
		argsHint= {
				"the triggering event, enclosed by { and }",
				"the list of plan terms corresponding to the code of the relevant plans",
				"the list of labels of the plans [optional]"
		},
		argsType= {
				"trigger",
				"list of plan terms",
				"list"
		},
		examples= {
				".relevant_plans({+!go(X,Y)},LP): unifies LP with all plans in the agent's plan library that are relevant for the triggering event +!go(X,Y)",
				".relevant_plans({+!go(X,Y)},LP, LL): same as above but also unifies LL with a list of labels of plans in LP",
				".relevant_plans({+!_},_,LL): gets the labels of all plans for achievement goals"
		},
		seeAlso= {
				"jason.stdlib.add_plan",
				"jason.stdlib.plan_label",
				"jason.stdlib.remove_plan"
		}
	)
@SuppressWarnings("serial")
public class relevant_plans extends DefaultInternalAction {

	@Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Trigger te = null;
        try {
            te = Trigger.tryToGetTrigger(args[0]);
        } catch (ParseException e) {}
        if (te == null)
            throw JasonException.createWrongArgument(this,"first argument '"+args[0]+"' must follow the syntax of a trigger.");

        ListTerm labels = new ListTermImpl();
        ListTerm lt = new ListTermImpl();
        ListTerm last = lt;
        if (!te.getLiteral().hasSource()) {
        	// the ts.relevantPlans requires a source to work properly
        	te.setLiteral(te.getLiteral().forceFullLiteralImpl());
        	te.getLiteral().addSource(new UnnamedVar());
        }
        List<Option> rp = ts.relevantPlans(te, null);
        if (rp != null) {
            for (Option opt: rp) {
                Plan np = (Plan)opt.getPlan().clone();
                // remove sources (this IA is used for communication) ==> moved to the kqmlPlans that answer askHow
                //if (np.getLabel() != null)
                //    np.getLabel().delSources();
                np.setAsPlanTerm(true);
                np.makeVarsAnnon();
                last = last.append(np);
                if (args.length == 3)
                    labels.add(np.getLabel());
            }
        }

        boolean ok = un.unifies(lt, args[1]); // args[1] is a var;
        if (ok && args.length == 3)
            ok = un.unifies(labels, args[2]);

        return ok;
    }
}
