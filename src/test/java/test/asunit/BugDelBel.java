package test.asunit;

import jason.asunit.TestAgent;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/** bug reported by Amandine
 * I have in my belief base one belief that I want to remove : mybelief(abc)[annot1(d)]
If I do -mybelief(_), the belief is not removed because  if ( ! t1s.hasSubsetAnnot(t2s, this)) of Unifier line 317 returns false with t1s=mybelief(abc)[annot1(d)] and t2s=mybelief(_) (called by line 962 in Agent)
If I do -mybelief(abc), the belief is removed because this time the variable bl of DefaultBeliefBase line 173 is not null (which is normal) and then annots are analyzed with if (l.hasSubsetAnnot(bl)) with l=mybelief(abc) and bl=mybelief(abc)[annot1(d)]. Here we can notice that the order of evaluation of hasSubsetAnnot is in reverse order compared to the previous case.

 */
public class BugDelBel {

    TestAgent ag1, ag2, oa, ob;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag1 = new TestAgent("a");

        ag1.parseAScode(
            "mybelief(abc)[annot1(d)]. "+
            "+!start1 <- -mybelief(abc)." +
            "+!start2 <- -mybelief(_)."
        );

        ag2 = new TestAgent("a2");
        ag2.parseAScode(
            "mybelief(abc)[source(robot)]. "+
            "+!start1 <- -mybelief(abc)[source(_)]."
        );

    }

    @Test(timeout=2000)
    public void test1() {
        int s = ag1.getBB().size();
        ag1.addGoal("start1");
        ag1.assertIdle(10);
        assertEquals(s-1, ag1.getBB().size());
    }

    @Test(timeout=2000)
    public void test2() {
        int s = ag1.getBB().size();
        ag1.addGoal("start2");
        ag1.assertIdle(10);
        assertEquals(s-1, ag1.getBB().size());
    }

    @Test(timeout=2000)
    public void test3() {
        int s = ag1.getBB().size();
        ag2.addGoal("start1");
        ag2.assertIdle(10);
        assertEquals(s-1, ag2.getBB().size());
    }

}
