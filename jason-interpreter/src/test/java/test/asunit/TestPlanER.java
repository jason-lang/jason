package test.asunit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import jason.asunit.TestAgent;


public class TestPlanER {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!test  <- !g1(5); endtest. "+

            "+a(X) <- aroot(X)." +
            "+c(X) <- croot(X)." +
            "+d(X) <- droot(X).\n" +

            "+!g1(X) <: g1(X) <- inig1. { \n"+
            "    +a(X) : X > 2 <- a1ing1; +g1(X); .print(a); a2ing1; .print(b)." +
            "} " +

            "+!g2(X) <- +a(X); !sg2(X+1). { "+
            "    +!sg2(Y) <- !ssg2(4). { "+
            "       +!ssg2(Z) <- +a(Y+Z); +b(10); +c(33); +c(0). "+
            "       +b(A)  <- binsg2(A+X). "+   // use var from scope
            "    }"+
            "    +b(A)  <- bing2(A). "+
            "    +c(A) : A < X <- cing2(A). "+
            "}\n"+

            "+!subgoalreturn " +
            "   <: false" +
            "   <- !sg1(X);" +
            "      inig1. " +
            "   {" +
            "      +!sg1(X) <- X = 10 + 3. " +
            "   }"

        );
    }

    @Test(timeout=2000)
    public void testReturnFromSubGoal() {
        ag.addGoal("subgoalreturn");
        ag.assertAct("inig1", 10);
    }

    @Test(timeout=2000)
    public void testGC1() {
        ag.addGoal("test");
        ag.assertAct("inig1", 10);
        ag.assertNoAct("endtest", 5);
    }

    @Test(timeout=2000)
    public void testGC2() {
        ag.addGoal("test");
        ag.assertNoAct("endtest", 10);
        ag.addBel("g1(5)");
        ag.assertAct("endtest", 10);
        ag.addBel("a(1)");
        ag.assertAct("aroot(1)", 10);
        ag.addBel("a(5)");
        ag.assertAct("aroot(5)", 10);
        ag.assertNoAct("a1ing1", 10);
        ag.assertIdle(10);
    }

    @Test(timeout=2000)
    public void testVarContext1() {
        ag.addGoal("test");
        ag.assertAct("inig1", 10);
        ag.addBel("a(1)");
        ag.assertNoAct("a1ing1", 10);
        ag.assertAct("aroot(1)", 10);
    }

    @Test(timeout=2000)
    public void testExtEvt1() {
        ag.addGoal("test");
        ag.assertAct("inig1", 10);
        assertEquals(1, ag.getTS().getC().getPendingIntentions().size());
        ag.addBel("a(5)"); // should trigger both +a/1 in root and inside g1
        ag.assertAct("aroot(5)", 10);
        ag.assertAct("a1ing1", 10);
        ag.assertIdle(10);
    }

    @Test(timeout=2000)
    public void testExtEvt2() {
        ag.addGoal("test");
        ag.assertAct("inig1", 10);
        ag.addBel("d(5)");
        ag.assertNoAct("xxxx", 20); // just to run all possible code
        //System.out.println(ag.getArch().getActions());
        // droot(5) should be executed once
        assertEquals("[inig1, droot(5)]",ag.getArch().getActions().toString());
    }

    @Test(timeout=4000)
    public void testSubPlan1() {
        ag.addGoal("test");
        ag.assertNoAct("endtest", 20);
        ag.addBel("a(5)");
        ag.assertAct("aroot(5)", 10);
        ag.assertAct("a1ing1", 10);
        ag.assertNoAct("xxxx", 20); // just to run all possible code
        //System.out.println(ag.getArch().getActions());
        ag.assertNoAct("a2ing1", 10); // not performed, since the GC is satisfied
        ag.assertAct("endtest", 10);
    }

    @Test(timeout=2000)
    public void testScope1() {
        ag.addGoal("g2(1)");
        ag.assertAct("aroot(1)", 10);
        ag.assertAct("aroot(6)", 10);
        ag.assertAct("binsg2(11)", 10);
        ag.assertAct("croot(33)", 10); // +c/1 in scope of !g2 is not applicable, use +c/1 from root
        ag.assertAct("cing2(0)", 10);
        //System.out.println(ag.getArch().getActions());
        assertEquals("[aroot(1), aroot(6), binsg2(11), croot(33), cing2(0)]",ag.getArch().getActions().toString());
    }
}
