package test;

import jason.asSemantics.Agent;
import jason.asSemantics.InternalAction;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

public class FunctionsTest extends TestCase {
    public void testRandom() throws Exception {
        Agent ag = new Agent();
        ag.initAg();

        new jason.stdlib.set_random_seed().execute(ag.getTS(), new Unifier(), new Term[] { ASSyntax.parseNumber("20") });

        InternalAction ia_r = (InternalAction) ag.getIA(".random");

        double retFunc = new jason.functions.Random().evaluate(ag.getTS(), new Term[] { ASSyntax.parseNumber("10") });
        assertEquals(7.320, retFunc, 0.01);

        Unifier u1 = new Unifier();
        VarTerm X = new VarTerm("X");
        ia_r.execute(ag.getTS(), u1, new Term[] { X });
        assertEquals(0.206, Double.parseDouble(u1.get("X").toString()), 0.01);

        Unifier u2 = new Unifier();
        ia_r.execute(ag.getTS(), u2, new Term[] { X });
        assertEquals(0.793, Double.parseDouble(u2.get("X").toString()), 0.01);

        Unifier u3 = new Unifier();
        ia_r.execute(ag.getTS(), u3, new Term[] { X });
        assertEquals(0.163, Double.parseDouble(u3.get("X").toString()), 0.01);
    }
}
