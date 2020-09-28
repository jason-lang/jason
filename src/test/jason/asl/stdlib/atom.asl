/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_atom
    <-
    !assert_false( .atom(b(10)) );
    !assert_true( .atom(b) );
    !assert_false( .atom(~b) );
    !assert_false( .atom(10) );
    !assert_false( .atom("home page") );
    !assert_false( .atom(X) );

    Y = "a string";
    !assert_false( .atom(Y) );

    Z = atom;
    !assert_true( .atom(Z) );

    !assert_false( .atom(a(X)) );
    !assert_false( .atom(a[X]) );
    !assert_false( .atom([a,b,c]) );
    !assert_false( .atom([a,b,c(X)]) );
.
