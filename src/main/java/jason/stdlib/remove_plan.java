package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/**
  <p>Internal action: <b><code>.remove_plan</code></b>.

  <p>Description: removes plans from the agent's plan library.

  <p>Parameters:<ul>

  <li>+ label(s) (structure or list of structures): the label of the
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

  <li> <code>.relevant_plans({ +!g }, _, L); .remove_plan(LL)</code>:
  removes all plans with trigger event <code>+!g</code>.</li>
  </ul>

  @see jason.stdlib.add_plan
  @see jason.stdlib.plan_label
  @see jason.stdlib.relevant_plans

 */
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

        Term source = BeliefBase.ASelf;
        if (args.length > 1) {
            source = (Atom)args[1];
        }
        if (label.isList()) { // arg[0] is a list
            for (Term t: (ListTerm)args[0]) {
                //r = r && ts.getAg().getPL().remove((Atom)t, source);
                ts.getAg().getPL().remove((Atom)t, source);
            }
        } else { // args[0] is a plan label
            ts.getAg().getPL().remove((Atom)label, source);
        }
        return true;
    }
}
