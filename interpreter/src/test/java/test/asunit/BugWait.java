package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** bug reported by Igor
 *
 * https://github.com/jason-lang/jason/issues/12
 */
public class BugWait {

    TestAgent ag, oa, ob;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "day(saturday). "+
            "!start. "+
            "+!start <- .wait(day(saturday)); jason.asunit.print(hello)."
        );
    }

    @Test(timeout=2000)
    public void testProg() {
        ag.addGoal("start");
        ag.assertPrint("hello", 10);
    }

}
