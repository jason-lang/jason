/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_ground
    <-
    !assert_true( .ground(b(10)) );
    !assert_true( .ground(b) );
    !assert_true( .ground(~b) );
    !assert_true( .ground(10) );
    !assert_true( .ground("home page") );
    !assert_false( .ground(X) );

    Y = "a string";
    !assert_true( .ground(Y) );

    Z = atom;
    !assert_true( .ground(Z) );

    !assert_false( .ground(a(X)) );
    !assert_false( .ground(a[X]) );
    !assert_true( .ground([a,b,c]) );
    !assert_false( .ground([a,b,c(X)]) );
.
