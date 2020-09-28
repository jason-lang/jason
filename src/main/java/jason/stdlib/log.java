package jason.stdlib;

import java.util.logging.Level;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;


/**
  <p>Internal action: <b><code>.log(level, args...)</code></b>.

  <p>Description: used for printing messages to the log

  <p>Examples:<ul>
  <li> <code>.log(info,"Created artifact counter",N)</code>: prints to the log the referred message.</li>
  </ul>

  @see https://docs.oracle.com/javase/tutorial/java/data/numberformat.html
  @see jason.stdlib.print

*/
@Manual(
        literal=".log(level,arg0[,arg1,...])",
        hint="used for logging messages",
        argsHint= {
          "log level: severe, warning, info, fine, finer or finest (see java.util.logging)",
          "the terms to be logged",
          "the term to be concatenated with prior one [optional]",
        },
        argsType= {
                "term",
                "term",
                "term"
        },
        examples= {
                ".log(info,\"Created artifact counter\"): prints to the log the referred message with level INFO."
        },
        seeAlso= {
                "jason.stdlib.print",
                "jason.stdlib.println"
        }
    )
@SuppressWarnings("serial")
public class log extends DefaultInternalAction {

    private static InternalAction singleton = null;

    public static InternalAction create() {
        if (singleton == null)
            singleton = new log();
        return singleton;
    }

    protected String getNewLine() {
        return "\n";
    }

    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isAtom())
            throw JasonException.createWrongArgument(this,"first argument must be an atom: severe, warning, info, fine, finer or finest");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        StringBuilder sout = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            if (args[i].isString()) {
                StringTerm st = (StringTerm)args[i];
                sout.append(st.getString());
            } else {
                Term t = args[i];
                if (! t.isVar()) {
                    sout.append(t);
                } else {
                    sout.append(t+"<no-value>");
                }
            }
        }

        Level level;
        switch (((Atom)args[0]).getFunctor()) {
        case "severe": level = Level.SEVERE; break;
        case "warning": level = Level.WARNING; break;
        case "fine": level = Level.FINE; break;
        case "finer": level = Level.FINER; break;
        case "finest": level = Level.FINEST; break;
        default:
            level = Level.INFO; break;
        }

        if (ts != null) {
            ts.getLogger().log(level, sout.toString());
        } else {
            System.out.print(sout.toString() + getNewLine());
        }

        return true;
    }
}
