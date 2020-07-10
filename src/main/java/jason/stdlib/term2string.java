
package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;

/**
  <p>Internal action: <b><code>.term2string(T,S)</code></b>.

  <p>Description: converts the term T into a string S and vice-versa.

  <p>Parameters:<ul>
  <li>-/+ T (any term).<br/>
  <li>-/+ S (a string).<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.term2string(b,"b")</code>: true.
  <li> <code>.term2string(b,X)</code>: unifies X with "b".
  <li> <code>.term2string(X,"b")</code>: unifies X with b.
  <li> <code>.term2string(X,"10")</code>: unifies X with 10 (a number term).
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.reverse

*/
@Manual(
        literal=".term2string(term,string)",
        hint="converts the term into a string and vice-versa",
        argsHint= {
                "the term",
                "the string"
        },
        argsType= {
                "term",
                "string"
        },
        examples= {
                ".term2string(b,\"b\"): true",
                ".term2string(b,X): unifies X with \"b\"",
                ".term2string(X,\"b\"): unifies X with b",
                ".term2string(X,\"10\"): unifies X with 10 (a number term)"
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.delete",
                "jason.stdlib.length",
                "jason.stdlib.reverse"
        }
    )
@SuppressWarnings("serial")
public class term2string extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new term2string();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        // case 1, no vars
        if (!args[0].isVar() && args[1].isString()) {
            return args[0].toString().equals( ((StringTerm)args[1]).getString() );
        }

        // case 2, second is var
        if (!args[0].isVar() && args[1].isVar()) {
            return un.unifies(new StringTermImpl(args[0].toString()), args[1]);
        }

        // case 3, first is var
        if (args[0].isVar()) {
            String tAsString;

            if (args[1].isString()) {
                tAsString = ((StringTerm)args[1]).getString();
            } else {
                tAsString = args[1].toString();
            }

            try {
                // tries to parse it as an usual term
                return un.unifies(args[0], ASSyntax.parseTerm( tAsString ));
            } catch (ParseException e) {
                // considers the string as special atom
                return un.unifies(args[0], new Atom( tAsString ));
            }
        }

        throw new JasonException("invalid case of term2string");
    }
}

