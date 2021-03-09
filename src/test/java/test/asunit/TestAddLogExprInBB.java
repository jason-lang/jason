package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by Neil Madden -- see jason list */
public class TestAddLogExprInBB {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        ag.setDebugMode(true);

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "vl(5). "+
            "!test. "+
            "+!test <- +b(10, (vl(X) & not X > 10)); "+ // X is a free var
            "          ?b(_,X); "+   // the name of the var must be X (to cause a "conflict" with bel internal vars)
            "          jason.asunit.print(a1). " + // the execution must not fail.

            "+!t2   <- ?b(_,X); "+
            "          ?(X); "+
            "          jason.asunit.print(X)."+

            "+!t3   <- ?b(_,X); "+
            "          ?(not X); "+
            "          jason.asunit.print(a2)."+

            "+!t4   <- ?b(_, (B & not X)); "+
            "          ?(B); B = vl(N); jason.asunit.print(X); "+ // the ?(B) also change the var inside X (in gprolog it works like that)
            "          act(N)."  +

            // failure test
            "+!t5   <- N = 1; ?(N > 1); jason.asunit.print(nok). "+
            "-!t5   <- jason.asunit.print(ok). "
        );
    }

    @Test(timeout=2000)
    public void testRule() {
        ag.assertBel("b(10, (vl(X) & not X > 10))", 5);
        ag.assertPrint("a1", 5);

        ag.addGoal("t2");
        ag.assertPrint("(vl(5) & not ((5 > 10)))", 5);

        ag.delBel("vl(5)");
        ag.addBel("vl(15)");
        ag.addGoal("t3");
        ag.assertPrint("a2", 5);
    }

    @Test(timeout=2000)
    public void testRule2() {
        ag.addGoal("t4");
        ag.assertAct("act(5)", 10);
        ag.assertPrint("5 > 10", 0);
    }

    @Test(timeout=2000)
    public void testRule3() {
        ag.addGoal("t5");
        ag.assertPrint("ok", 10);
    }
}
