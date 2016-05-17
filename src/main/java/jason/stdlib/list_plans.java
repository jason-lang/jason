package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
<p>Internal action: <b><code>.list_plans</code></b>.

<p>Description: prints out the plans in the plan library. 

<p>Parameter:<ul>
<li>+ trigger (trigger term, [optional]): list only plan that unifies this parameter as trigger event.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.list_plans</code>
<li> <code>.list_plans({ +g(_) })</code>
</ul>

*/
public class list_plans extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Trigger te = null;
        if (args.length == 1 && args[0] instanceof Trigger)
            te = Trigger.tryToGetTrigger(args[0]);
        
        for (Plan p: ts.getAg().getPL()) {
            if (!p.getLabel().toString().startsWith("kqml")) { // do not list kqml plans
                if (te == null || new Unifier().unifies(p.getTrigger(), te)) {
                    ts.getLogger().info(p.toString());
                }
            }
        }
        return true;
    }
}
