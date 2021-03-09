/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_structure
    <-
    !assert_true( .structure(b(10)) );
    .log(warning,"TODO: .structure(b) is supposed to be true!");
    //!assert_true( .structure(b) );
    !assert_false( .structure(10) );
    !assert_false( .structure("home page") );
    !assert_false( .structure(X) );

    Y = "a string";
    !assert_false( .structure(Y) );

    Z = atom;
    !assert_true( .structure(Z) );

    !assert_true( .structure(a(X)) );
    !assert_true( .structure([a,b,c]) );
    !assert_true( .structure([a,b,c(X)]) );
.
