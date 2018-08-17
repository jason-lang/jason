package jason.stdlib;

import java.util.logging.Level;

import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;


/**
  <p>Internal action: <b><code>.printf(format, args...)</code></b>.

  <p>Description: used for printing messages to the console inspired by Java printf/format.

  NB.: do not use "%d" since all numbers used by this internal action are translated from Jason to a Java double.
    
  <p>Examples:<ul>
  <li> <code>.printf("Value %08.0f%n",N)</code>: prints <code>Value 00461012</code>.</li>
  <li> <code>.printf("Value "%10.3f"",N)</code>: prints <code>Value      3.142</code>.</li>
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
            if (args[i].isNumeric()) { // no integers! (since .printf("%d",10.2) and .pring("%.2f",10) produces erros
                javaArgs[i-1] = 0;
                try {
                    javaArgs[i-1] = ((NumberTerm)args[i]).solve();
                } catch (NoValueException e) {
                    e.printStackTrace();
                }               
            }
        }
        String sout = String.format( ((StringTerm)args[0]).getString(), javaArgs);

        if (ts != null && ts.getSettings().logLevel() != Level.WARNING) {
            ts.getLogger().info(sout.toString());
        } else {
            System.out.print(sout.toString() + getNewLine());
        }

        return true;
    }
}
