/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("test_assert.asl") }

!execute_test_plans.

@test_launch_suspend[atomic]
+!test_launch_suspend
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

    .suspend(go(1,3));
    !assert_false(.desire(go(1,3)));
    !!test_suspend_goal;
.

+!test_suspend_goal
    <-
    !assert_true(suspended); // It is expected ^!go has added "suspended" to bb
    .print("TODO: It is expected ^!go has added 'suspended' to bb");
.

^!go(X,Y)[state(S)]
    <-
    +S;
.
