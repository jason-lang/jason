package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** bug reported by Luis Lampert
 *
 */
public class BugFor {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!start <- "+
            "     S = [a,b,c]; " +
            "     for ( .range(I,0,2)) {\n" +
            "       .nth(I,S,A);\n" +
            "       jason.asunit.print(A);\n" +
            "     };"  +
            "     jason.asunit.print(end)."
        );
    }

    @Test(timeout=2000)
    public void testProg() {
        ag.addGoal("start");
        ag.assertPrint("a", 10);
        ag.assertPrint("b", 10);
        ag.assertPrint("c", 10);
        ag.assertPrint("end", 10);
    }

}
