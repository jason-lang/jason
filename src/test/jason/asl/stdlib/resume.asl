/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

!execute_test_plans.

@test_launch_resume[atomic]
+!test_launch_resume
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(500); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan to test desire
    !!go(1,3);
    !assert_true(.desire(go(1,3)));

    .suspend(go(1,3));

    !!test_suspend_goal;
.

+!test_suspend_goal
    <-
    .log(warning,"TODO: test_suspend_goal has no current intention!");
    //!assert_true(state(suspended)); // It is expected ^!go has added 'suspended' to bb

    -state(executing);
    !assert_false(state(executing)); // executing was removed, should be inserted by meta event

    .resume(go(1,3));
    .log(warning,"TODO: test_suspend_goal has no current intention!");
    //!assert_true(.desire(go(1,3)));
    !assert_true(.intend(go(1,3)));

    !!test_resume_goal;
.


+!test_resume_goal
    <-
    !assert_true(state(executing)); // It is expected ^!go has added 'state(executing)' to bb
.

^!go(X,Y)[state(S)]
    <-
    +state(S);
.
