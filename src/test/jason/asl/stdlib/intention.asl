/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("test_assert.asl") }

!execute_test_plans.

@test_launch_intention[atomic]
+!test_launch_intention
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(50); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan
    !!go(1,3);
    !!go(2,3);
    !!test_intention;
.

// This plan should not be triggered by the manager
+!test_intention
    <-
    // It is expected to have 3 intentions
    .findall(i(I1,D1),.intention(I1,D1),L1);
    .length(L1,LL1);
    //.print("Intentions (id,state): ",L1);
    !assert_equals(3,LL1);

    // It is expected to have 1 running and two suspended
    .findall(I2,.intention(I2,running),L2);
    .length(L2,LL2);
    !assert_equals(1,LL2);

    // Get only the trigger of each intention extracting from means
    .findall(Trigger3,.intention(I3_1,_,[im(_,{+!Trigger3[_]},_,_)]),L3_1);
    !assert_equals([test_intention,go(1,3),go(2,3)],L3_1);

.
