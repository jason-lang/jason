package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by Jorgen Villadsen */
public class BugVarsAsGoal {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!g <- +p(ggg); !gg; Y={!ggg}; Y. " +
            "+!gg : p(X) <- +X; !!X. " +
            "+!ggg[source(A)] <- jason.asunit.print(A). "+
            "+!jig[scheme(S)]  <- jason.asunit.print(S)." +
            "+!jig  <- jason.asunit.print(noscheme)." +
            "+!test1(S) <- !run(jig,S)." +
            "+!test2(G) <- !G." +
            "+!run(G,S) <- !G[scheme(S)]."
        );
    }

    @Test(timeout=2000)
    public void testGoal() {
        ag.addGoal("g");
        ag.assertPrint("self", 10);
        ag.assertPrint("self", 10);
    }

    @Test(timeout=2000)
    public void test1() {
        ag.addGoal("test1(s1)");
        ag.assertPrint("s1", 10);
    }
    @Test(timeout=2000)
    public void test2() {
        ag.addGoal("test2(jig)");
        ag.assertPrint("noscheme", 10);
    }
}
