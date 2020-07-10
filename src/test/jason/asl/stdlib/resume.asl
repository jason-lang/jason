/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("test_assert.asl") }

!execute_test_plans.

@test_launch_resume[atomic]
+!test_launch_resume
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

    .resume(go(1,3));
    !assert_true(.desire(go(1,3)));

    .log(warning,"TODO: It is expected to keep the desire even when suspended");
    !!test_resume_goal;
.

+!test_resume_goal
    <-
    //!assert_true(resumed); // It is expected ^!go has added 'resumed' to bb
    .log(warning,"TODO: It is expected ^!go has added 'resumed' to bb");
.

^!go(X,Y)[state(S)]
    <-
    +S;
.
