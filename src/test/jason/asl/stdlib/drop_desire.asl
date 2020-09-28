/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_drop_desire
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
    !!go(2,3);
    !!go(2,4);
    !assert_true(.desire(go(1,3)));
    !assert_true(.desire(go(2,3)));
    !assert_true(.desire(go(2,4)));

    .drop_desire(go(X,3));

    !assert_false(.desire(go(1,3)));
    !assert_false(.desire(go(2,3)));
    !assert_true(.desire(go(2,4)));
.
