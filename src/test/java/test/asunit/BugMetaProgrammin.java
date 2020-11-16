package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** bugs reported by Stephen (by email)
 *
 * https://github.com/jason-lang/jason/issues/12
 */
public class BugMetaProgrammin {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!b1 <-\n" +
            "    PlanBody = { !g(a)[b] }; " +
            "    {BT;_} = PlanBody; " +
            "    jason.asunit.print(BT); " +
            "    BT = achieve(Arg);" +
            "    jason.asunit.print(Arg); " +
            "    BT =.. [achieve, Arg2, _];" +
            "    jason.asunit.print(Arg2). " +

            "+!b2 <- " +
            "    PB = {!g(a)[b]}; " +
            //"    PB = achieve(G); " +
            //"    G =.. [_,_,Annots2]; " +
            //"    jason.asunit.print(Annots2); "+
            "    PB = achieve(_[|Annots1]); " +
            "    jason.asunit.print(Annots1). "
        );
    }

    @Test(timeout=2000)
    public void bug1() {
        ag.addGoal("b1");
        ag.assertPrint("g(a)[b]", 10);
        ag.assertPrint("[g(a)[b]]", 10);
    }

    @Test(timeout=2000)
    public void bug2() {
        ag.addGoal("b2");
        ag.assertPrint("[b]", 10);
    }
}
