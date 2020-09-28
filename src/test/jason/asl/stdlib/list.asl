/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_list
    <-
    !assert_true( .list([a,b,c]) );
    !assert_true( .list([a,b,c(X)]) );
    !assert_false( .list(10) );
    !assert_false( .list("home page") );
    !assert_false( .list(X) );

    Y = "a string";
    !assert_false( .list(Y) );

    Z = [];
    !assert_true( .list(Z) );

    !assert_false( .list(a(X)) );
.
