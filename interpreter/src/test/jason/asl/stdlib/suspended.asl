/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_suspended
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

    !!test_waiting_goal;
.

+!test_waiting_goal
    <-
    .wait(100); // waits !go to execute .wait
    !assert_true(state(waiting)); // It is expected to be witing since go(X,Y) is always on .wait
    .suspend(go(1,3));
    !!test_suspended_goal;
.

+!test_suspended_goal
    <-
    .wait(100); // gives some time for the meta-event to be processed
    !assert_true(state(suspended));

    .suspended(go(1,3), wait(W)[time(T)]); // Just calling .suspended() from outside of a test
    !assert_equals(500,T);
.

^!go(X,Y)[state(S)]
    <-
    +state(S);
.
