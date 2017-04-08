package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.list_rules</code></b>.

<p>Description: prints out the rules in the belief base.

<p>Example:<ul>
<li> <code>.list_rules</code>
</ul>

*/
public class list_rules extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        for (Literal b: ts.getAg().getBB()) {
            if (b.isRule()) {
                ts.getLogger().info(b.toString());
            }
        }
        return true;
    }
}
