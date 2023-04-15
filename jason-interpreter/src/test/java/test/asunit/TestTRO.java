package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

// test Tail Recursion Optimization
public class TestTRO {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!run <- !fat(5,1,F); jason.asunit.print(F). "+
            "+!fat(1,V,V). "+
            "+!fat(N,A,X) <- !fat(N-1,N*A,X)."
        );
    }

    @Test(timeout=2000)
    public void testGoalSrouce() {
        ag.addGoal("run");
        ag.assertPrint("120", 50);
    }
}
