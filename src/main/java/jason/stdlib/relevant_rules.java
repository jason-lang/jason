// Internal action code for project tell-rule.mas2j

package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import jason.asSyntax.Term;

import java.util.Iterator;

/**
<p>Internal action: <b><code>.relevant_rules</code></b>.

<p>Description: gets all rules that can be used to prove some literal.

<p>Parameters:<ul>

<li>+ argument (literal): the argument to proof.</li>

<li>- rules (list of rule terms): the list of rules that prove the argument</li>

</ul>

<p>Example:<ul>

<li> <code>.relevant_rules(p(_),LP)</code>: unifies LP with a list of
all rules with head p/1.</li>


</ul>

*/
@Manual(
        literal=".relevant_rules(argument,rules)",
        hint="gets all rules that can be used to prove some literal",
        argsHint= {
                "the argument to proof",
                "the rules that prove it"
        },
        argsType= {
                "literal",
                "list of plan rules"
        },
        examples= {
                ".relevant_rules(p(_),LP): unifies LP with a list of all rules with head p/1."
        },
        seeAlso= {
                ""
        }
    )
@SuppressWarnings("serial")
public class relevant_rules extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Literal pattern = (Literal)args[0];
            ListTerm result = new ListTermImpl();
            synchronized (ts.getAg().getBB().getLock()) {
                Iterator<Literal> i = ts.getAg().getBB().getCandidateBeliefs(pattern, un);
                if (i != null) {
                    while (i.hasNext()) {
                        Literal l = i.next();
                        if (l.isRule()) {
                            if (un.clone().unifies(pattern, l)) {
                                l = l.copy();
                                l.delSources();
                                ((Rule)l).setAsTerm(true);
                                result.add(l);
                            }
                        }
                    }
                }
            }
            return un.unifies(args[1],result);
        } catch (Exception e) {
            ts.getLogger().warning("Error in internal action 'relevant_rules'! "+e);
        }
        return false;
    }
}

