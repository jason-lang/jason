package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

public class FunctionsTest extends TestCase {
    public void testRandom() throws Exception {
        new jason.stdlib.set_random_seed().execute(null, new Unifier(), new Term[] { ASSyntax.parseNumber("20") });

        double retFunc = new jason.functions.Random().evaluate(null, new Term[] { ASSyntax.parseNumber("10") });
        assertEquals(7.320, retFunc, 0.01);

        Unifier u1 = new Unifier();
        VarTerm X = new VarTerm("X");
        new jason.stdlib.random().execute(null, u1, new Term[] { X });
        assertEquals(0.206, Double.parseDouble(u1.get("X").toString()), 0.01);

        Unifier u2 = new Unifier();
        new jason.stdlib.random().execute(null, u2, new Term[] { X });
        assertEquals(0.793, Double.parseDouble(u2.get("X").toString()), 0.01);

        Unifier u3 = new Unifier();
        new jason.stdlib.random().execute(null, u3, new Term[] { X });
        assertEquals(0.163, Double.parseDouble(u3.get("X").toString()), 0.01);
    }
}
