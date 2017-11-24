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
            "q. r. b. " +
            "+!test1 <- .asserta(mybel(10)); .asserta( { p :- q & r } ); .asserta( { v(X,Y) :- Y=X+10 } ). " +
            "+!test2 : p <- jason.asunit.print(2). " +
            "+!test3(X) : v(X,Y) <- jason.asunit.print(Y). \n" +
            "+!test4  <- +{ v :- p & v(2,12) }; !test4a. " +
            "+!test4a : v <- jason.asunit.print(ok). " +
            "+!test4a     <- jason.asunit.print(bu). \n" +

            "+!test5  <- +{ +!k <- jason.asunit.print(ok) }; !k. " +

            "r1 :- b. "+
            "r2[a1,a2] :- q & r." +
            "+!test6 : r1[an] <- jason.asunit.print(nok). "+
            "+!test6 : r1 <- jason.asunit.print(ok). "+
            "+!test7 : r2[a1] <- jason.asunit.print(nok). "+
            "+!test7  <- jason.asunit.print(ok). "+
            "+!test8 : r2[a1] <- jason.asunit.print(ok). "
        );
    }

    @Test(timeout=2000)
    public void testRule1() {
        ag.assertBel("q", 20);
        ag.assertBel("q[source(self)]", 20);
        ag.addGoal("test1");
        ag.assertBel("p", 20);
        ag.assertBel("p[source(self)]", 20);
        ag.assertBel("mybel(10)[source(self)]", 10);
        ag.assertIdle(10);
        
        ag.addGoal("test2");
        ag.assertPrint("2", 10);

        ag.addGoal("test3(2)");
        ag.assertPrint("12", 10);

        ag.addGoal("test4");
        ag.assertPrint("ok", 5);
    }

    @Test(timeout=2000)
    public void testRule2() {
        ag.addGoal("test6");
        ag.assertPrint("ok", 15);
        ag.addGoal("test7");
        ag.assertPrint("ok", 15);
        ag.addGoal("test8");
        ag.assertPrint("ok", 15);
    }

    @Test(timeout=2000)
    public void testPlan() {
        ag.addGoal("test5");
        ag.assertPrint("ok", 10);
    }

}
