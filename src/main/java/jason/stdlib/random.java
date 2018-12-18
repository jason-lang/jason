package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Iterator;
import java.util.Random;

/**
  <p>Internal action: <b><code>.random(<i>N</i>)</code></b>.

  <p>Description: unifies <i>N</i> with a random number between 0 and 1.

  <p>Parameter:<ul>

  <li>- value (number): the variable to receive the random value<br/>
  <li><i>+ quantity of random numbers</i> (number, optional): default value is 1, value = 0 means that an infinify number of random numbers will be produced (this is useful for some backtrack circumstances).</li>

  </ul>

  <p>Example:<ul>

  <li><code>.random(X)</code>: unifies X with one random number between 0 and 1.</li>
  <li><code>.random(X, 5)</code>: unifies X with a random number between 0 and 1, and backtracks 5 times. For example: .findall(X, .random(X,5), L) will produce a list of 5 random numbers.</li>
  <li><code>.random(X, 0)</code>: unifies X with a random number between 0 and 1, and backtracks infinitely.</li>

  </ul>

  @see jason.functions.Random function version

*/
public class random extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new random();
        return singleton;
    }

    private Random random = new Random();

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument must be a variable.");
        if (args.length == 2 && !args[1].isNumeric())
            throw JasonException.createWrongArgument(this,"second argument must be a number.");
    }

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        if (args.length == 1) {
            return un.unifies(args[0], new NumberTermImpl(random.nextDouble()));
        } else {
            final int max = (int)((NumberTerm)args[1]).solve();

            return new Iterator<Unifier>() {
                int n = 0;
                // we always have a next random number
                public boolean hasNext() {
                    return (n < max || max == 0) && ts.getUserAgArch().isRunning();
                }
                public Unifier next() {
                    Unifier c = un.clone();
                    c.unifies(args[0], new NumberTermImpl(random.nextDouble()));
                    n++;
                    return c;
                }
                public void remove() {}
            };

        }
    }
}
