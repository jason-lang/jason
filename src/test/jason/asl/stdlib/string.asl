/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_string
    <-
    !assert_true( .string("home page") );
    !assert_false( .string(b(10)) );
    !assert_false( .string(b) );
    !assert_false( .string(X) );

    Y = "a string";
    !assert_true( .string(Y) );

    Z = atom;
    !assert_false( .string(Z) );
.
