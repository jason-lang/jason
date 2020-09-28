/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_eval
    <-
    .eval(X1, true | false);
    !assert_true(X1);

    .eval(X2, 3<5 & not 4+2<3);
    !assert_true(X2);
.
