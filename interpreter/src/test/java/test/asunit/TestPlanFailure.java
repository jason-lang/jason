package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestPlanFailure {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!test  : true <- !g1(X); endtest; end(X). "+

            "+!g1(X) : true <- inig1; !g2(X); endg1. "+
            "+!g2(X) : true <- inig2; !g3(X); endg2. "+
            "+!g3(X) : true <- inig2; !g4(X); endg3. "+
            "+!g4(X) : true <- inig4; !g5(X); endg4. "+
            "+!g5(_) : true <- .fail. "+
            "-!g3(failure)[error(ia_failed)] : true <- infailg3. "+

            "+!norel <- !j; endnorel. "+
            "-!j[error(no_relevant)] <- infailj. " +

            "+!s <- ?bufferReady; jason.asunit.print(ok)." +
            "+?bufferReady : a     <- jason.asunit.print(oka). " +
            "+?bufferReady : not a <- .fail.  " +
            "-?bufferReady <- jason.asunit.print(w); ?bufferReady."

        );
    }

    @Test(timeout=2000)
    public void testFailurePlan() {
        ag.addGoal("test");
        ag.assertAct("inig4", 10);
        ag.assertAct("infailg3", 5);
        ag.assertAct("endg2", 5);
        ag.assertAct("endtest", 5);
        ag.assertAct("end(failure)", 5);
    }

    @Test(timeout=2000)
    public void testNoRelPlan() {
        ag.addGoal("norel");
        ag.assertAct("infailj", 10);
        ag.assertAct("endnorel", 5);
    }

    @Test(timeout=2000)
    public void testTestGoalFailure() {
        ag.addGoal("s");
        ag.assertPrint("w", 10);
        ag.assertPrint("w", 10);
        ag.assertPrint("w", 10);
        ag.addBel("a");
        ag.assertPrint("oka", 10);
        ag.assertPrint("ok", 10);
    }

}
