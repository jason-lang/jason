/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_abolish
    <-

    +b(1);
    +b("test");
    .count(b(_),N1);
    !assert_equals(2,N1);
    .abolish(b(_));
    .count(b(_),N2);
    !assert_equals(0,N2);

    +c(a,b)[source(ag1)];
    +c(a,c)[source(ag2)];
    +c(a,t)[source(ag1)];
    .count(c(_,t),N3);
    !assert_equals(1,N3);
    .abolish(c(_,t));
    .count(c(_,t),N4);
    !assert_equals(0,N4);
    .count(c(_,_)[source(ag1)],N5);
    !assert_equals(1,N5);
    .abolish(c(_,_)[source(ag1)]);
    .count(c(_,_)[source(ag1)],N6);
    !assert_equals(0,N6);
    .count(c(_,_),N7);
    !assert_equals(1,N7);
    .abolish(_[source(ag2)]);
    .count(_[source(ag2)],N8);
    !assert_equals(0,N8);
.
