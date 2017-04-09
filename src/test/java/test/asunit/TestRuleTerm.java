package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestRuleTerm {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "q. r. " +
            "+!test1 <- .asserta( { p :- q & r } ); .asserta( { v(X,Y) :- Y=X+10 } ). " +
            "+!test2 : p <- jason.asunit.print(2). " +
            "+!test3(X) : v(X,Y) <- jason.asunit.print(Y). \n" +
            "+!test4  <- +{ v :- p & v(2,12) }; !test4a. " +
            "+!test4a : v <- jason.asunit.print(ok). " +
            "+!test4a     <- jason.asunit.print(bu). \n" +

            "+!test5  <- +{ +!k <- jason.asunit.print(ok) }; !k. "
        );
    }

    @Test(timeout=2000)
    public void testRule() {
        ag.addGoal("test1");
        ag.assertBel("p", 5);

        ag.addGoal("test2");
        ag.assertPrint("2", 5);

        ag.addGoal("test3(2)");
        ag.assertPrint("12", 5);

        ag.addGoal("test4");
        ag.assertPrint("ok", 5);
    }

    @Test(timeout=2000)
    public void testPlan() {
        ag.addGoal("test5");
        ag.assertPrint("ok", 10);
    }

}
