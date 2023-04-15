package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class BugSucceedGoal {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "!g. +!g <- !go; jason.asunit.print(end). "+
            "+!go <- !go2; jason.asunit.print(a3). "+
            "+!go2 <- .succeed_goal(go2); jason.asunit.print(a4). "
        );
    }


    @Test(timeout=2000)
    public void test1() {
        ag.addGoal("g");
        ag.assertPrint("a3", 30);
        ag.assertPrint("end", 30);
    }
}
