/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("test_assert.asl") }

!execute_test_plans.

@test_launch_succeed_goal[atomic]
+!test_launch_succeed_goal
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(10); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan to test desire
    !!go(1,3);
    !assert_true(.desire(go(1,3)));

    .succeed_goal(go(1,3));
    !assert_false(.desire(go(1,3)));
    !!test_succeed_goal;
.

+!test_succeed_goal
    <-
    //!assert_true(finished); // It is expected ^!go has added "finished" to bb
    .print("TODO: It is expected ^!go has added 'finished' after succeed_goal");
.

^!go(X,Y)[state(S)]
    <-
    +S;
.
