package jason.asSyntax.directives;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSemantics.Agent;
import jason.asSemantics.ArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.functions.*;

/**
 * This class maintains the set of arithmetic functions available for the AS parser.
 *
 * @author Jomi
 */
public class FunctionRegister extends DefaultDirective implements Directive {
    static Logger logger = Logger.getLogger(FunctionRegister.class.getName());

    private final static Map<String,ArithFunction> functions = new HashMap<>();

    // add known global functions (can be computed without an agent reference)
    static {
        addJasonFunction(Abs.class);
        addJasonFunction(acos.class);
        addJasonFunction(asin.class);
        addJasonFunction(atan.class);
        addJasonFunction(Average.class);
        addJasonFunction(ceil.class);
        addJasonFunction(cos.class);
        addJasonFunction(e.class);
        addJasonFunction(floor.class);
        addJasonFunction(Length.class);
        addJasonFunction(log.class);
        addJasonFunction(Max.class);
        addJasonFunction(mean.class);
        addJasonFunction(median.class);
        addJasonFunction(Min.class);
        addJasonFunction(pi.class);
        addJasonFunction(Random.class);
        addJasonFunction(Round.class);
        addJasonFunction(sin.class);
        addJasonFunction(Sqrt.class);
        addJasonFunction(StdDev.class);
        addJasonFunction(Sum.class);
        addJasonFunction(tan.class);
        addJasonFunction(time.class);
    }

    private static void addJasonFunction(Class<? extends ArithFunction> c) {
        try {
            ArithFunction af = c.getConstructor().newInstance();
            functions.put(af.getName(), af);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registering function "+c.getName(),e);
        }
    }

    /** add new global function (shared among all agents in the JVM) */
    public static void addFunction(Class<? extends ArithFunction> c) {
        try {
            ArithFunction af = c.getConstructor().newInstance();
            String error = FunctionRegister.checkFunctionName(af.getName());
            if (error != null)
                logger.warning(error);
            else
                functions.put(af.getName(), af);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registering function "+c.getName(),e);
        }
    }

    public static String checkFunctionName(String fName) {
        if (functions.get(fName) != null)
            return "Can not register the function "+fName+"  twice!";
        else if (!fName.contains("."))
            return "The function "+fName+" was not registered! A function must have a '.' in its name.";
        else if (fName.startsWith("."))
            return "The function "+fName+" was not registered! An user function name can not start with '.'.";
        else
            return null;
    }

    public static ArithFunction getFunction(String function, int arity) {
        ArithFunction af = functions.get(function);
        if (af != null && af.checkArity(arity))
            return af;
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        if (outerContent == null)
            return null;
        try {
            String id = ((StringTerm)directive.getTerm(0)).getString();
            if (directive.getArity() == 1) {
                // it is implemented in java
                outerContent.addFunction((Class<ArithFunction>)Class.forName(id));
            } else if (directive.getArity() == 3) {
                // is is implemented in AS
                int arity = (int)((NumberTerm)directive.getTerm(1)).solve();
                String predicate = ((StringTerm)directive.getTerm(2)).getString();
                outerContent.addFunction(id, arity, predicate);
            } else {
                // error
                logger.log(Level.SEVERE, "Wrong number of arguments for register_function "+directive);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing directive register_function.",e);
        }
        return null;
    }
}
