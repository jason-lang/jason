package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class BugDelVar {

    TestAgent ag;

    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "a(10). +!start <- -a(X); jason.asunit.print(X). "
        );
    }

    @Test(timeout=3000)
    public void testGoal() {
        ag.addGoal("start");
        ag.assertPrint("10", 10);
    }
}
