package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class BugIfLength {

    TestAgent ag, oa, ob;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "b(b). "+
            "+!start <- .my_name(Me); "+
            "if (Me == a) { "+
            ".findall(A,b(A),AS); L = [Me|AS]; jason.asunit.print(L,.length(L)) "+
            "}."
        );
    }

    @Test(timeout=2000)
    public void testProg() {
        ag.addGoal("start");
        ag.assertPrint("[a,b]2", 10);
        ag.addGoal("start"); // repeat the test
        ag.assertPrint("[a,b]2", 10);
        ag.addBel("b(c)");
        ag.addGoal("start"); // repeat the test
        ag.assertPrint("[a,c,b]3", 10);
    }

}
