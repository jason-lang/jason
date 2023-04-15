package jason.stdlib;

import java.util.Random;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/**
 <p>Internal action: <b><code>.set_random_seed(<i>N</i>)</code></b>.

 <p>Description: sets the seed of Jason's random number generator

 <p>Parameter:<ul>

 <li>- seed (number): the value to which the random seed is to be set</li>

 </ul>

 <p>Example:<ul>

 <li><code>.set_random_seed(20)</code>: Sets the random number generator's seed to 20.</li>
 </ul>

 @see jason.functions.Random
 @see jason.stdlib.random

 */
@Manual(
        literal="math.set_random_seed(seed)",
        hint="generates a random number between 0 and 1",
        argsHint= {
                "sets the seed of Jason's random number generator"
        },
        argsType= {
                "number"
        },
        examples= {
                ".set_random_seed(20): sets the random number generator's seed to 20"
        },
        seeAlso= {
                "jason.functions.Random",
                "jason.stdlib.random"
        }
)

public class set_random_seed extends DefaultInternalAction {

    public static Random getAgRandomIA(TransitionSystem ts) throws Exception {
        random r_ia = (random)ts.getAg().getIA(".random");
        return r_ia.getRandomGenerator();
    }

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        Long seed = (long) ((NumberTerm)args[0]).solve();
        //RandomSingleton.setSeed(seed);
        getAgRandomIA(ts).setSeed(seed);
        return true;
    }
}
