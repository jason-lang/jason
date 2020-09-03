/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_launch_drop_intention
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(50); // An arbitrary delay
    }, self, begin);

    /**
     * Add a mock plan for to_be_dropped
     */
    .add_plan({
      +!to_be_dropped <-
          .drop_intention
    }, self, begin);

    // Trigger the mock plan
    !!go(1,3);
    !!go(2,3);
    !!to_be_dropped; // should not appear on list of intentions
    !!test_drop_intention;
.

// This plan should not be triggered by the manager
+!test_drop_intention
    <-
    // Get all triggers
    .findall(Trigger1,.intention(I1_1,_,[im(_,{+!Trigger1[_]},_,_)]),L1_1);
    !assert_equals([test_drop_intention,go(1,3),go(2,3)],L1_1);

    .drop_intention(go(1,3));

    // Get all triggers, again
    .findall(Trigger2,.intention(I2_1,_,[im(_,{+!Trigger1[_]},_,_)]),L2_1);
    !assert_equals([test_drop_intention,go(2,3)],L2_1);
.
