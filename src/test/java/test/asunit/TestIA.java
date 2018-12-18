package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Assert;
import org.junit.Test;

public class TestIA {

    @Test(timeout=2000)
    public void testAddPlan() {
        TestAgent ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!test   <- act1. \n"+
            "+!add    <- .add_plan(\"@l0 +!test <- act0.\", bob, begin). "+
            "+!remove <- .remove_plan(l0, bob). "
        );
        ag.addGoal("test");
        ag.assertAct("act1", 10);
        ag.addGoal("add");
        ag.assertIdle(30);
        ag.addGoal("test");
        ag.assertAct("act0", 10);
        ag.addGoal("remove");
        int size = ag.getPL().size();
        ag.assertIdle(30);
        Assert.assertTrue(size-1 == ag.getPL().size());
        ag.addGoal("test");
        ag.assertAct("act1", 10);
    }

}
