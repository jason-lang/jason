package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;

/**
<p>Internal action: <b><code>.list_plans</code></b>.

<p>Description: prints out the plans in the plan library.

<p>Parameter:<ul>
<li>+ trigger (trigger term -- optional): list only plan that unifies this parameter as trigger event.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.list_plans</code>: list all agent's plans
<li> <code>.list_plans({ +g(_) })</code>: list agent's plans that unifies with +g(_)
</ul>

*/
@Manual(
        literal=".list_plans[(trigger)]",
        hint="prints out the plans in the plan library",
        argsHint= {
                "list only plan that unifies this parameter as trigger event"
        },
        argsType= {
                "trigger term"
        },
        examples= {
                ".list_plans: list all agent's plans",
                ".list_plans({ +g(_) }): list agent's plans that unifies with +g(_)"
        },
        seeAlso= {
                ""
        }
    )
@SuppressWarnings("serial")
public class list_plans extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (args.length == 1 && args[0] instanceof Trigger) {
            Trigger te = Trigger.tryToGetTrigger(args[0]);
            if (!te.getLiteral().hasSource()) {
                te.getLiteral().addSource(new UnnamedVar());
            }

            for (Plan p: ts.getAg().getPL()) {
                //if (!p.getLabel().toString().startsWith("kqml")) { // do not list kqml plans
                    if (te == null || new Unifier().unifies(p.getTrigger(), te)) {
                        ts.getLogger().info(p.toString());
                    }
                //}
            }
        } else {
            ts.getLogger().info(ts.getAg().getPL().getAsTxt(false));
        }
        return true;
    }
}
