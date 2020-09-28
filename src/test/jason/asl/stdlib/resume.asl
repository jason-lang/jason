/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_resume_goal
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(200); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan to test desire
    !!go(1,3);

    .suspend(go(1,3));
    .wait(50); // Give some time to answer to the generated event '.suspend(G)'
    !assert_true(state(suspended)); // It is expected ^!go has added 'suspended' to bb

    -state(executing);
    !assert_false(state(executing)); // executing was removed, should be inserted by meta event

    .resume(go(1,3));

    .wait(100); // Give some time to answer to the generated event '.resume(G)'
    !assert_true(.desire(go(1,3)));
    !assert_true(.intend(go(1,3)));
    !assert_true(state(resumed)); // It is expected ^!go has added 'state(executing)' to bb
.

^!go(X,Y)[state(S)]
    <-
    +state(S);
.
