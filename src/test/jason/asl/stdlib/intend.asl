/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("test_assert.asl") }

!execute_test_plans.

@test_intend[atomic]
+!test_intend
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(10); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan
    !!go(1,3);
    !!go(2,3);

    // Print intentions
    .findall(D,.intend(D),L);
    //.print("Intentions: ",L);

    // There is an open issue regarding .intend
    .print("TODO: see issue #44 https://github.com/jason-lang/jason/issues/44")
.
