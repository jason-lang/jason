package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestLoop {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "b(1). "+
            "p(1,a). p(2,a). p(3,b). p(4,b). p(6,a).\n "+
            "+!test1 <- \n" +
            "    while( .count(b(_),N) & N < 4) {" +
            "        +b(N+1);" +
            "    }."+

            "+!test2 <- L=4; while( .count(b(_)) < L) { ?b(X); +b(X+1) }; jason.asunit.print(end).\n "+ // old syntax, for compatibility test

            "+!test2p <- L=4; " +
            "     while( .count(b(_),LL) & LL < (L+3)) { " +
            "         ?b(X); +b(X+1);" +
            "      }" +
            "      jason.asunit.print(end).\n "+

            "+!test3 <- L=4; for( p(N,a) & N < L) { jason.asunit.print(N) }; jason.asunit.print(end).\n "+

            "+!test4 <- for( .member(N, [1,3,4]) ) { jason.asunit.print(N) }; jason.asunit.print(end).\n " + // old syntax, for compatibility test

            "+!test5 <- for( .range(I, 1, 4) ) { " +
            "                jason.asunit.print(I);" +
            "           } " +
            "           for( .member(I, [a,b,c]) ) { " +
            "                jason.asunit.print(I);" +
            "           } " +
            "           jason.asunit.print(end). "
        );
    }

    @Test(timeout=2000)
    public void testWhile1() {
        ag.addGoal("test1");
        ag.assertBel("b(4)", 20);
    }

    @Test(timeout=2000)
    public void testWhile2p() {
        ag.addGoal("test2p");
        ag.assertBel("b(6)", 30);
        ag.assertPrint("end", 30);
    }

    @Test(timeout=2000)
    public void testWhile2() {
        ag.addGoal("test2");
        ag.assertBel("b(4)", 30);
        ag.assertPrint("end", 30);
    }

    @Test(timeout=2000)
    public void testFor1() {
        ag.addGoal("test3");
        ag.assertPrint("2", 10);
        ag.assertPrint("end", 10);
    }

    @Test(timeout=2000)
    public void testFor2() {
        ag.addGoal("test4");
        ag.assertPrint("4", 10);
        ag.assertPrint("end", 10);
    }

    @Test(timeout=2000)
    public void testFor3() {
        ag.addGoal("test5");
        ag.assertPrint("1", 5);
        ag.assertPrint("2", 5);
        ag.assertPrint("3", 5);
        ag.assertPrint("4", 5);
        ag.assertPrint("a", 5);
        ag.assertPrint("b", 5);
        ag.assertPrint("c", 5);
        ag.assertPrint("end", 10);
    }
}
