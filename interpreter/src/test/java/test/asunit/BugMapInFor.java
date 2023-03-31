package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** bug reported by Skouras Konst */

public class BugMapInFor {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
"""
+!initializeMap(N) <-
   .map.create(MO);
   .map.put(MO,1,2);
   for(.range(I,1,N)) {
      .map.put(MO,I,I);
      //.print("After put Map is ", MO);
   };
   jason.asunit.print(MO).

+!test_return <-           // it works (the bug is not related to "return")
   .map.create(MO);
   .map.put(MO,1,2);
   !t(MO);
   jason.asunit.print(MO).

+!t(MO) <-
   .map.put(MO,2,55).


+!initializeMapNoFor(MO,N) <- // it works (usual iteration without for works)
   .map.create(MO);
   .map.put(MO,1,2);
   !initializeMapNoFor(MO,1,N);
   jason.asunit.print(MO).
+!initializeMapNoFor(MO,V,N) : V > N.
+!initializeMapNoFor(MO,V,N) <-
   .map.put(MO,V,V);
   //.print("After put Map is ", MO);
   !initializeMapNoFor(MO,V+1,N).

+!test_copy <-
   .map.create(MO);
   .map.put(MO,1,2);
   .map.put(MO,2,55);
   .map.copy(MO,Copy);
   .map.put(Copy,3,11);
   jason.asunit.print(MO);
   jason.asunit.print(Copy).

"""
        );
    }

    @Test(timeout=2000)
    public void testLoopFor() {
        ag.addGoal("initializeMap(3)");
        ag.assertPrint("{1->1,2->2,3->3}", 50);
    }

    @Test(timeout=2000)
    public void testLoopNoFor() {
        ag.addGoal("initializeMapNoFor(_,3)");
        ag.assertPrint("{1->1,2->2,3->3}", 50);
    }

    @Test(timeout=2000)
    public void testRet() {
        ag.addGoal("test_return");
        ag.assertPrint("{1->2,2->55}", 30);
    }

    @Test(timeout=2000)
    public void testCopy() {
        ag.addGoal("test_copy");
        ag.assertPrint("{1->2,2->55}", 30);
        ag.assertPrint("{1->2,2->55,3->11}", 30);
    }

}
