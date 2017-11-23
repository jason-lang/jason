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

            "+!g1(X) <: g1(X) { <- inig1. "+
            "    +a(X) : X > 2 <- a1ing1; +g1(X); a2ing1." +
            "} " +

            "+!g2(X) { <- +a(X); !sg2. "+
            "    +!sg2 { <- !ssg2. "+
            "       +!ssg2 <- +a(55); +b(10); +c(33). "+
            "       +b(X)  <- binsg2(X). "+
            "    }"+
            "    +b(X)  <- bing2(X). "+
            "    +c(X) : X < 10 <- cing2(X). "+
            "}"
        );
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
    }

    @Test(timeout=2000)
    public void testSubPlan1() {
        ag.addGoal("test");
        ag.assertNoAct("endtest", 10);
        ag.addBel("a(5)");
        ag.assertAct("aroot(5)", 10);
        System.out.println(ag.getArch().getOutput());
        ag.assertAct("a1ing1", 10);
        //assertTrue(ag.getPL().hasPlansWithGoalCondition());
        ag.assertNoAct("a2ing1", 10); // not performed, since the GC is satisfied
        ag.assertAct("endtest", 10);
    }

    @Test(timeout=2000)
    public void testScope1() {
        ag.addGoal("g2(1)");
        ag.assertAct("aroot(1)", 10);
        ag.assertAct("aroot(55)", 10);
        ag.assertAct("binsg2(10)", 10);
        ag.assertAct("croot(33)", 10); // +c/1 in scope of !g2 is not applicable, use +c/1 from root
        //System.out.println(ag.getArch().getActions());        
        assertEquals("[aroot(1), aroot(55), binsg2(10), croot(33)]",ag.getArch().getActions().toString());
    }
}
