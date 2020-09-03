/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_namespace
    <-
    !assert_false( .namespace(non_existing) );

    +a(1);
    +family::brother(bob);
    +financial::account(1234);

    !assert_true( .namespace(family) );
    !assert_true( .namespace(financial) );
.
