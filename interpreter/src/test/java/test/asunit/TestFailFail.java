package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestFailFail {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!g  <- !g0; jason.asunit.print(goodend). "+
            "+!g0 <- !g1; jason.asunit.print(end). "+
            "-!g0 <- jason.asunit.print(solved). "+
            "+!g1 <- !g2; jason.asunit.print(bug). "+
            "-!g1 <- jason.asunit.print(try); .fail. "+
            "+!g2 <- .fail. "
        );
    }


    @Test(timeout=2000)
    public void test1() {
        ag.addGoal("g");
        ag.assertPrint("try", 30);
        ag.assertPrint("solved", 30);
        ag.assertPrint("goodend", 30);
    }

}
