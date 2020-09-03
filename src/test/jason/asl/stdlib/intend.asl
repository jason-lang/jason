/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_intend
    <-
    /**
     * Add a mock plan for go(X,Y)
     */
    .add_plan({
      +!go(X,Y) <-
          .wait(300); // An arbitrary delay
    }, self, begin);

    !assert_false(.intend(go(1,3)));

    // Trigger the mock plan
    !!go(1,3);

    !assert_true(.intend(go(1,3)));

    // Print intentions
    .findall(D,.intend(D),L);
    .log(info,"Intentions: ",L);
.
