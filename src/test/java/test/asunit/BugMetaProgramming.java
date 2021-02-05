package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** bugs reported by Stephen (by email)
  */
public class BugMetaProgramming {

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
                        "    BT = {!Arg};" +
                        "    jason.asunit.print(Arg); " +
                        "    BT =.. [achieve, Arg2, _];" +
                        "    jason.asunit.print(Arg2). " +

                        "+!b2 <- " +
                        "    PB = {!g(a)[b]}; " +
                        "    PB = { !G[|Annots1] }; " +
                        "    jason.asunit.print(Annots1). " +

                        "+!b3 <- " +
                        "    PB = {!g(a)[b]}; " +
                        "    PB = { !G }; " +
                        "    jason.asunit.print(k,G,k); "+
                        "    G =.. [_,_,Annots2]; " +
                        "    jason.asunit.print(i,Annots2,i). "
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

    @Test(timeout=2000)
    public void bug3() {
        ag.addGoal("b3");
        ag.assertPrint("kg(a)[b]k", 10);
        ag.assertPrint("i[b]i", 10);
    }
}
