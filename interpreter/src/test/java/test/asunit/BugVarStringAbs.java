package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by Marcelo Menegol */
public class BugVarStringAbs {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!start(X) : Y=math.abs(3*X) <- jason.asunit.print(Y). "
        );
    }

    @Test(timeout=2000)
    public void testAbs() {
        ag.addGoal("start(3.5)");
        ag.assertPrint("10.5", 10);

        ag.addGoal("start(\"a\")");
        ag.assertPrint("NaN", 10);
    }

}
