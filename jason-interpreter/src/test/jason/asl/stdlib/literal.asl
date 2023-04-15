/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_literal
    <-
    !assert_true( .literal(b(10)) );
    !assert_true( .literal(b) );
    !assert_false( .literal(10) );
    !assert_false( .literal("Jason") );
    !assert_false( .literal(X) );

    Y = "a string";
    !assert_false( .literal(Y) );

    Z = atom;
    !assert_true( .literal(Z) );

    !assert_true( .literal(a(X)) );
    !assert_false( .literal([a,b,c]) );
    !assert_false( .literal([a,b,c(X)]) );
.
