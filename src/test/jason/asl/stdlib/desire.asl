/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("test_assert.asl") }

!execute_test_plans.

@test_desire[atomic]
+!test_desire
    <-
    !assert_false(.desire(go(1,3)));

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

    /**
     * Trigger another plan that is actually becomes
     * a desire and instantly is fulfilled
     */
    !go(4,6);
    !assert_false(.desire(go(4,6)));

    // Print desires
    .findall(D,.desire(D),L);
    .print("Desires: ",L);
.
