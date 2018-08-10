package jason.stdlib;

import java.util.logging.Level;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;


/**
  <p>Internal action: <b><code>.printf(format, args...)</code></b>.

  <p>Description: used for printing messages to the console inspired by Java printf/format.

  <p>Examples:<ul>
  <li> <code>.printf("Value %08d%n",N)</code>: prints <code>Value 00461012</code>.</li>
  <li> <code>.printf("Value "%10.3f%n"",N)</code>: prints <code>Value      3.142</code>.</li>
  </ul>

  @see https://docs.oracle.com/javase/tutorial/java/data/numberformat.html
  @see jason.stdlib.print

*/
public class printf extends println {

    private static InternalAction singleton = null;
    
    public static InternalAction create() {
        if (singleton == null)
            singleton = new printf();
        return singleton;
    }
    
    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isString())
            throw JasonException.createWrongArgument(this,"first argument must be a string (the format)");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Object[] javaArgs = new Object[args.length-1];
        for (int i=1; i<args.length; i++) {
            javaArgs[i-1] = ASSyntax.termToObject(args[i]);
        }
        String sout = String.format(args[0].toString(), javaArgs);

        if (ts != null && ts.getSettings().logLevel() != Level.WARNING) {
            ts.getLogger().info(sout.toString());
        } else {
            System.out.print(sout.toString() + getNewLine());
        }

        return true;
    }
}
