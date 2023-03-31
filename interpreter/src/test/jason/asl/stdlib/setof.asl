/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_setof
    <-
    +c(100);
    +c(200);
    +c(100);

    .setof(X,c(X),L2);
    !assert_equals([100,200],L2);
.

@[test]
+!test_against_findall
    <-
    +a(30);
    +a(20);
    +b(1,2);
    +b(3,4);
    +b(5,6);

    .setof(X,a(X),L1);
    !assert_equals([30,20],L1);

    .setof(c(Y,X),b(X,Y),L2);
    !assert_equals([c(2,1),c(4,3),c(6,5)],L2);

    .setof(r(X,V1,V2), (a(X) & b(V1,V2) & V1*V2 < X), L3);
    !assert_equals([r(30,1,2),r(30,3,4),r(20,1,2),r(20,3,4)],L3);
.
