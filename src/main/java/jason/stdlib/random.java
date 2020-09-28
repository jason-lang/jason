package jason.stdlib;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.random(<i>N</i>)</code></b>.

  <p>Description: unifies <i>N</i> with a random number between 0 and 1.

  <p>Parameter:<ul>

  <li><i>+ options></i> (list, optional): the list of possible values, default values are any real number<br/>
  <li>- value (number): the variable that unifies with a random value from options<br/>
  <li><i>+ quantity of random values</i> (number, optional): default value is 1, value = 0 means that an infinity number of random values will be produced (this is useful for some backtrack circumstances).</li>

  </ul>

  <p>Example:<ul>

  <li><code>.random(X)</code>: unifies X with one random number between 0 and 1.</li>
  <li><code>.random(X, 5)</code>: unifies X with a random number between 0 and 1, and backtracks 5 times. For example: .findall(X, .random(X,5), L) will produce a list of 5 random numbers.</li>
  <li><code>.random(X, 0)</code>: unifies X with a random number between 0 and 1, and backtracks infinitely.</li>

  <li><code>.random([a,b,c],X)</code>: unifies X with a random value from a, b, or c.</li>
  </ul>

  @see jason.functions.Random function version

*/
@Manual(
        literal=".random(value)",
        hint="generates a random number between 0 and 1",
        argsHint= {
                "the variable to receive the random value"
        },
        argsType= {
                "number"
        },
        examples= {
                ".random(X): unifies X with one random number between 0 and 1",
                ".random(X, 5): unifies X with a random number between 0 and 1, and backtracks 5 times. For example: .findall(X, .random(X,5), L) will produce a list of 5 random numbers",
                ".random(X, 0): unifies X with a random number between 0 and 1, and backtracks infinitely"
        },
        seeAlso= {
                "jason.functions.Random"
        }
    )
@SuppressWarnings("serial")
public class random extends DefaultInternalAction {

    private Random random = new Random();

    public Random getRandomGenerator() {
        return random;
    }

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args[0].isList()) {
            if (!args[1].isVar())
                throw JasonException.createWrongArgument(this,"second argument must be a variable.");
            if (args.length == 3 && !args[2].isNumeric())
                throw JasonException.createWrongArgument(this,"third argument must be a number.");

        } else {
            if (!args[0].isVar())
                throw JasonException.createWrongArgument(this,"first argument must be a variable.");
            if (args.length == 2 && !args[1].isNumeric())
                throw JasonException.createWrongArgument(this,"second argument must be a number.");
        }
    }

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        if (args.length == 1) {
            return un.unifies(args[0], new NumberTermImpl(random.nextDouble()));
        } else {
            final ListTerm l;
            final int      max;
            if (args[0].isList()) {
                l = (ListTerm)args[0];
                if (l.isEmpty())
                    return false;
                if (args.length == 2) {
                    max = 1;
                } else {
                    max = (int)((NumberTerm)args[2]).solve();
                }
            } else {
                l   = null;
                max = (int)((NumberTerm)args[1]).solve();
            }
            return new Iterator<Unifier>() {
                int n = 0;
                List<Term> j = (l == null ? null : l.getAsList());

                public boolean hasNext() {
                    return (n < max || max == 0) && ts.getAgArch().isRunning();
                }

                public Unifier next() {
                    Unifier c = un.clone();
                    if (l == null)
                        c.unifies(args[0], new NumberTermImpl(random.nextDouble()));
                    else
                        c.unifies(args[1], j.get(random.nextInt(j.size())));
                    n++;
                    return c;
                }
                public void remove() {}
            };

        }
    }
}
