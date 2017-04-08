package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestGoalSource {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!begin <- !g; !g[source(bob)]. "+
            "+!g[source(A)] <- jason.asunit.print(A)."
        );
    }

    @Test(timeout=2000)
    public void testGoalSrouce() {
        ag.addGoal("begin");
        ag.assertPrint("self", 5);
        ag.assertPrint("bob", 5);
    }
}
