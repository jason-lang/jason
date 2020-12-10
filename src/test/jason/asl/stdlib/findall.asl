/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_findall
    <-
    +a(30);
    +a(20);
    +b(1,2);
    +b(3,4);
    +b(5,6);

    .findall(X,a(X),L1);
    !assert_equals([30,20],L1);

    .findall(c(Y,X),b(X,Y),L2);
    !assert_equals([c(2,1),c(4,3),c(6,5)],L2);

    .findall(r(X,V1,V2), (a(X) & b(V1,V2) & V1*V2 < X), L3);
    !assert_equals([r(30,1,2),r(30,3,4),r(20,1,2),r(20,3,4)],L3);
.

@[test]
+!test_against_setof
    <-
    +c(100);
    +c(200);
    +c(100);

    .findall(X,c(X),L1);
    !assert_equals([100,200,100],L1);
.

@[test]
+!test_against_setof
    <-
    +c(100);
    +c(200);
    +c(100);

    .findall(X,c(X),L1);
    !assert_equals([100,200,100],L1);
.

/**
 * This test is a clone of a test in test/java/test/StdLibTest.java file
 */
@[test]
+!test_clone_of_junit
    <-
    +a(10,x);
    +a(20,y);
    +a(30,x);
    .count(a(_,_),C);
    !assert_equals(3,C);

    .findall(X,a(X,x),L);
    !assert_equals(2,.length(L));
.
