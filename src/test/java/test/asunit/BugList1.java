package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class BugList1 {

    TestAgent ag, oa, ob;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!start <- L=\"[a]\"; !g(L). "+
            "+!g([]) <- jason.asunit.print(no)."+
            "+!g(_) <- jason.asunit.print(yes)."
        );
    }

    @Test(timeout=2000)
    public void testProg() {
        ag.addGoal("start");
        ag.assertPrint("yes", 10);
    }

}
