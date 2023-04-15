/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_range
    <-

    !assert_true( .range(3,1,5) );
    !assert_false( .range(6,1,5) );

    for ( .range(X,1,5) ) {
        !assert_contains([1,2,3,4,5], X);
    }

    for ( .range(X,-3,1) ) {
        !assert_contains([-3,-2,-1,0,1], X);
    }
.
