/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

!execute_test_plans.

@test_launch_fail_goal[atomic]
+!test_launch_fail_goal
    <-
    /**
     * Add a mock plan for go(X,Y)
     * First adding a -!go plan and later the +!go
     * which will be put on 'begin', exactly above
     * -!go
     */
     .add_plan({
         -!go(X,Y) <- .wait(10); // An arbitrary delay
     }, self, begin);
    .add_plan({
        +!go(X,Y) <- .wait(10); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan to test desire
    !!go(1,3);
    !assert_true(.desire(go(1,3)));

    .fail_goal(go(1,3));
    !assert_false(.desire(go(1,3)));
    !!test_fail_goal;
.

+!test_fail_goal
    <-
    .log(warning,"TODO: test_fail_goal has no current intention!");
    //!assert_true(state(failed)); // It is expected ^!go has added 'state(failed)' to bb
.

^!go(X,Y)[state(S)]
    <-
    +state(S);
.
