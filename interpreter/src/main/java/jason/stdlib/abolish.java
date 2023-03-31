package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.abolish</code></b>.

  <p>Description: removes all beliefs that match the argument. As for the
  "-" operator, an event will be generated for each deletion.
  Different from the "-" operator, which consider "self" as the default source, .abolish will ignore the source if not informed.

  <p>Parameters:<ul>
  <li>+ pattern (literal or variable): the "pattern" for what should be removed.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.abolish(b(_))</code>: remove all <code>b/1</code> beliefs, regardless of the argument value and the source of the belief.</li>
  <li> <code>.abolish(c(_,t))</code>: remove all <code>c/2</code> beliefs where the second argument is <code>t</code>.</li>
  <li> <code>.abolish(c(_,_)[source(ag1)])</code>: remove all <code>c/2</code> beliefs that have <code>ag1</code> as source.</li>
  <li> <code>.abolish(_[source(ag1)])</code>: remove any beliefs that have <code>ag1</code> as source.</li>
  </ul>

  @see jason.stdlib.asserta
  @see jason.stdlib.assertz
  @see jason.stdlib.belief

 */
@Manual(
        literal=".abolish(argument)",
        hint="removes all beliefs that match the argument.",
        argsHint= {
                "the \"pattern\" for what should be removed."
        },
        argsType= {
                "literal or variable"
        },
        examples= {
                ".abolish(b(_)): remove all b/1 beliefs, regardless of the argument value and the source of the belief",
                ".abolish(c(_,t)): remove all c/2 beliefs where the second argument is t",
                ".abolish(c(_,_)[source(ag1)]): remove all c/2 beliefs that have ag1 as source",
                ".abolish(_[source(ag1)]): remove any beliefs that have ag1 as source"
        },
        seeAlso= {
                "jason.stdlib.asserta",
                "jason.stdlib.assertz"
        }
    )
@SuppressWarnings("serial")
public class abolish extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral() & !args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument must be a literal or variable.");
    }

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        ts.getAg().abolish((Literal)args[0], un);
        return true;
    }

}
