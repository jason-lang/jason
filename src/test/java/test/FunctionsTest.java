package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

public class FunctionsTest extends TestCase {
    public void testRandom() throws Exception {
        VarTerm X = new VarTerm("X");
        Unifier u = new Unifier();
        new jason.stdlib.set_random_seed().execute(null, u, new Term[] { ASSyntax.parseNumber("0.15")});
        assertEquals(0.730967787376657, new jason.functions.Random().evaluate(null, new Term[] { X }));
    }
}
