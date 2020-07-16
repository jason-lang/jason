/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

!execute_test_plans.

@test_intend[atomic]
+!test_intend
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(300); // An arbitrary delay
    }, self, begin);

    // Trigger the mock plan
    !!go(1,3);
    !!go(2,3);

    .wait(100); // wait plans to start
    // Print intentions
    .findall(D,.intend(D),L);
    .log(info,"Intentions: ",L);

    // There is an open issue regarding .intend
    .log(warning,"TODO: the test_intent should not be run as atomic, otherwise the goals go/2 will not start and so will not be intended");
.
