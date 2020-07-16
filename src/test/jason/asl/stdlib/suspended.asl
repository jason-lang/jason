/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

!execute_test_plans.

@test_suspended[atomic]
+!test_suspended
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

    !!test_waiting_goal;
.

+!test_waiting_goal
    <-
    !assert_true(state(waiting)); // It is expected to be suspended since go(X,Y) is always on .wait

    .suspend(go(1,3));
    !!test_suspended_goal;
.

+!test_suspended_goal
    <-
    //!assert_true(state(suspended)); // It is expected ^!go has added 'state(waiting)' to bb
    .log(warning,"TODO: It is expected ^!go has added 'state(suspended)' to bb");

    .suspended(go(1,3),R2); // Just calling .suspended() from outside of a test

    //!assert_false(.string(R2));
    .log(warning,"TODO: It is expected not a string after .suspended(_,R) unification");

    //R2 = wait(W)[time(T)];
    //!assert_equals(10,T);
.

^!go(X,Y)[state(S)]
    <-
    +state(S);
.
