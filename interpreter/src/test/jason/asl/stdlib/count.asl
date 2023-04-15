/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_count
    <-

    .count(a(2,_),N1);
    !assert_equals(0,N1);

    +a(2,1);
    +a(2,6);
    +a(2,8);
    +a(2,10);
    +a(2,20);
    .count(a(2,_),N2);
    !assert_equals(5,N2);

    .count((a(2,X) & X>10),N3);
    !assert_equals(1,N3);

    !assert_true( .count(a(2,_),5) );
    !assert_false( .count(a(2,_),6) );
.
