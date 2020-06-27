package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class BugFail {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!g0 <- !g1; jason.asunit.print(end). "+
            "+!g1 <- !g2; jason.asunit.print(bug). "+
            "-!g1 <- jason.asunit.print(ok). "+
            "+!g2 <- .fail. " +

            "+!t([]). "+
            "+!t([Item|List]) <- .print(\"Getting \",Item); !g2; volta; !t(List). "+
            "-!t([Item|List]) <- .print(\"Throwing away \",Item);       !t(List). "
        );
    }


    @Test(timeout=2000)
    public void test1() {
        ag.addGoal("g0");
        ag.assertPrint("ok", 30);
        ag.assertPrint("end", 30);
    }

    @Test(timeout=2000)
    public void test2() {
        ag.addGoal("t([banana,abacaxi,pera])");
        ag.assertNoAct("volta", 20);
    }

}
