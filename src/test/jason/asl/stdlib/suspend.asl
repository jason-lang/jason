/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_launch_suspend
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(300); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan to test desire
    !!go(1,3);
    !assert_true(.desire(go(1,3)));

    .suspend(go(1,3));
    !assert_true(.desire(go(1,3)));
    !!test_suspend_goal;
.

+!test_suspend_goal
    <-
    .wait(50);
    !assert_true(suspended); // It is expected ^!go has added "suspended" to bb
.

^!go(X,Y)[state(S)]
    <-
    +S;
.
