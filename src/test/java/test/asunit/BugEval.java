package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by Grimaldo */
public class BugEval {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!start  <- A1 = false;     B1 = true;     .eval(X1, A1 | B1);     jason.asunit.print(X1); "+
            "            !a2(A2);     !b2(B2);   .eval(X2, A2 | B2);     jason.asunit.print(X2). "+
            "+!a2(false). +!b2(true). "
        );
    }

    @Test(timeout=2000)
    public void test() {
        ag.addGoal("start");
        ag.assertPrint("true", 10);
        ag.assertPrint("true", 10);
    }

}
