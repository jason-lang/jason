package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class BugIfGoalReturn {

    TestAgent ag, oa, ob;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");
        oa = new TestAgent("oa");
        ob = new TestAgent("ob");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "scheme(pass_fence_sch,a)[owner(oa)]. "+
            "scheme(pass_fence_sch,b)[owner(ob)]. " +
            "+!start : true "+
            "<- .findall(PFSch, scheme(pass_fence_sch,PFSch), PFSchs); "+
            "   !find_pass_fence_scheme(PFSchs, 1, 1, OtherSch, Porter2); "+
            "   jason.asunit.print(OtherSch,Porter2). "+

            "+!find_pass_fence_scheme([],SX,SY,no_scheme,no_porter). "+
            "+!find_pass_fence_scheme([Sch|Others], SX,SY, PassSch, Porter2) "+
            "  : scheme(_,Sch)[owner(Oag)] "+
            "<-  jason.asunit.print(Oag); "+
            "    .send(Oag, askOne, goal_state(_,pass_fence(SX,SY,_,_),_), Ans); "+
            "    if (Ans == false) { "+
            "       jason.asunit.print(Others); "+
            "       !find_pass_fence_scheme(Others,SX,SY,PassSch,Porter2) "+
            "    } else { "+
            "       jason.asunit.print(asking_porter); "+
            "       .send(Oag, askOne, play(_,gatekeeper2,_), play(Porter2,_,_)); "+
            "       jason.asunit.print(Porter2); "+
            "       PassSch = Sch"+
            "    }; "+
            "    jason.asunit.print(endofind)."
        );

        oa.parseAScode("");
        ob.parseAScode(
            "goal_state(a,pass_fence(1,1,a,a),a). play(bob,gatekeeper2,gr1). "
        );
    }

    @SuppressWarnings("deprecation")
    @Test(timeout=2000)
    public void testRule() {
        ag.addGoal("start");
        ag.assertPrint("oa", 10);
        ag.assertIdle(30); // goes until .send askone
        junit.framework.Assert.assertEquals(1, ag.getTS().getC().getPendingIntentions().size());
        junit.framework.Assert.assertFalse(oa.getTS().canSleep());
        //oa.getTS().getUserAgArch().getArchInfraTier().checkMail();
        oa.assertIdle(30); // gives time to oa to respond
        ag.assertPrint("[b]", 10);
        ag.assertPrint("ob", 10);
        ag.assertIdle(30); // goes until .send askone
        ob.assertIdle(30); // gives time to oa to respond
        ag.assertPrint("asking_porter", 10);
        ag.assertIdle(30);
        ob.assertIdle(30); // communication time
        ag.assertPrint("bob", 10);
        ag.assertPrint("endofind", 10);
        ag.assertPrint("endofind", 10);
        ag.assertPrint("bbob", 10);
    }

}
