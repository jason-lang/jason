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
            "+!test  <- !g1(X); endtest. "+

            "+!g1(X) <: g1(X) { <- inig1. "+
            "    +a(X) : X > 10 <- +g1(X). }"

        );
    }

    @Test(timeout=2000)
    public void testGC() {
        ag.addGoal("test");
        ag.assertAct("inig1", 10);
        //ag.assertNoAct("endtest", 5);
    }

}
