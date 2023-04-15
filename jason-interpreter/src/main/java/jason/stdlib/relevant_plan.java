package jason.stdlib;

import java.util.Iterator;
import java.util.List;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.parser.ParseException;


/**
  <p>Internal action: <b><code>.relevant_plan</code></b>.

  <p>Description: gets relevant plans for some triggering event. This is a backtracking based version of .relevant_plans.

  <p>Parameters:<ul>

  <li>+/- trigger (trigger): the triggering event, enclosed by { and }.</li>

  <li>+/- plan  (plan as term): a relevant plan for the trigger.</li>

  </ul>

  <p>Example:<ul>

  <li> <code>.relevant_plan({+!go(X,Y)},P)</code>: unifies P with all plans that are relevant for the triggering
  event <code>+!go(X,Y)</code>. To find all plans the internal action .findall can be used to backtrack on all solutions:
  <code>.findall(P, .relevant_plan({+!go(X,Y)},P), ListOfPlans)</code>.

  To find all plans from file source "a.asl":
  <code>
  .findall(P, .relevant_plan({+!go(X,Y)},P]) & .plan_label(P,L[file("a.asl")), ListPlans)
  </code>

  </li>

  </ul>

  @see jason.stdlib.add_plan
  @see jason.stdlib.plan_label
  @see jason.stdlib.remove_plan
  @see jason.stdlib.relevant_plans
 */
@SuppressWarnings("serial")
public class relevant_plan extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        if (args[0].isVar() && !args[1].isVar()) {
            // case to get the Trigger of a plan
            return un.unifies(args[0], ((Plan)args[1]).getTrigger());
        }

        Trigger te = null;
        try {
            te = Trigger.tryToGetTrigger(args[0]);
        } catch (ParseException e) {        }
        if (te == null)
            throw JasonException.createWrongArgument(this,"first argument '"+args[0]+"' must follow the syntax of a trigger.");

        if (!te.getLiteral().hasSource()) {
            // the ts.relevantPlans requires a source to work properly
            te.setLiteral(te.getLiteral().forceFullLiteralImpl());
            te.getLiteral().addSource(new UnnamedVar());
        }
        List<Plan> rp = ts.getAg().getPL().getCandidatePlans(te);
        if (rp == null)
            return false;

        final Trigger fte = te;
        return new Iterator<Unifier>() {
            Iterator<Plan> i = rp.iterator();
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
                    c = p.isRelevant(fte, (Unifier)un.clone());
                    if (c != null && c.unifiesNoUndo(args[1], p)) // p.getLabel()))
                        return;
                }
                c = null; // no member is found,
            }
        };
    }
}
