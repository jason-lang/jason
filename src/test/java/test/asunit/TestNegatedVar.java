package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestNegatedVar {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        ag.setDebugMode(true);

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!start <- +b. " +
            "+B <- !~B. " +
            "+!X <- jason.asunit.print(X)."
        );
    }

    @Test(timeout=2000)
    public void testContext() {
        ag.addGoal("start");
        ag.assertPrint("~b[source(self)]", 5);
    }

}
