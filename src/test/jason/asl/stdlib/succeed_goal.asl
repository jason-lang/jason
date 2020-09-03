/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_succeed_goal
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

    .succeed_goal(go(1,3));
    .wait(100);
    !assert_false(.desire(go(1,3)));

    .wait(100);
    !assert_true(state(finished)); // It is expected ^!go has added "finished" to bb
.

^!go(X,Y)[state(S)]
    <-
    +state(S);
.
