package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.empty</code></b>.

  <p>Description: checks whether the argument does not have any term.

  <p>Parameters:<ul>
  <li>+ argument (string or list): the term whose length is to be determined.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.empty([])</code>: true.
  <li> <code>.empty([a,b])</code>: false, the list has at least one term.
  <li> <code>.empty("a")</code>: false, the argument has at least one term.
  </ul>

 */
@Manual(
        literal=".empty(argument)",
        hint="checks whether the argument does not have any term",
        argsHint= {
                "the term whose length is to be determined"
        },
        argsType= {
                "string or list"
        },
        examples= {
                ".empty([]): true",
                ".empty([a,b]): false, the list has at least one term",
                ".empty(\"a\"): false, the argument has at least one term"
        },
        seeAlso= {
                ""
        }
    )
@SuppressWarnings("serial")
public class empty extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new empty();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        Term l1 = args[0];
        if (l1.isList()) {
            ListTerm lt = (ListTerm) l1;
            return lt.isEmpty();
        } else if (l1.isString()) {
            StringTerm st = (StringTerm) l1;
            return st.getString().isEmpty();
        }

        NumberTerm s = length.getSize(l1);
        if (s != null && ((int)s.solve()) == 0)
            return true;

        return false;
    }
}
