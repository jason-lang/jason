/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_number
    <-
    !assert_false( .number(b(10)) );
    !assert_false( .number(b) );
    !assert_false( .number(~b) );
    !assert_true( .number(10) );
    !assert_false( .number("home page") );
    !assert_false( .number(X) );

    Y = "a string";
    !assert_false( .number(Y) );

    Z = -400;
    !assert_true( .number(Z) );

    !assert_false( .number(a(X)) );
    !assert_false( .number([1,2]) );
.
