package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestPlanER {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!test  <- !g1(5); endtest. "+

            "+!g1(X) <: g1(X) { <- inig1. "+
            "    +a(X) : X > 2 <- .print(here); +g1(X). }"

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
    }

    @Test(timeout=2000)
    public void testSubPlan1() {
        ag.addGoal("test");
        ag.assertNoAct("endtest", 10);
        ag.addBel("a(5)");
        ag.assertAct("endtest", 10);
    }

}
