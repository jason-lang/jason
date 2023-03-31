/**
 * Test lists
 */

{ include("tester_agent.asl") }

member(Item, [Item|Tail]).
//member(Item, [Head|Tail]) :- member(Item,Tail).

l([a,b,c(0),1]).
a_rule(X,RET) :- l(L) & .reverse(L,LREV) & LREV = [H|T] & RET = H + X.
another_rule(Y,RET2) :- a_rule(Y,RET) & RET > Y & RET2 = RET.

/**
 * Test
 */
@[test]
+!test_lists
    <-
    [HX|TX] = [a,b,c(0),1];
    !assert_equals(a,HX);
    !assert_equals([b,c(0),1],TX);

    L = [a,b,c(0),1];
    L = [H|T];
    !assert_equals(a,H);
    !assert_equals([b,c(0),1],T);
    [a,b,c(0),1] = [Hb|Tb];
    !assert_equals(a,Hb);
    !assert_equals([b,c(0),1],Tb);

    q([X,Y|R]) = q([a,b,c(0),1]);
    !assert_equals(a,X);
    !assert_equals(b,Y);
    !assert_equals([c(0),1],R);

    // Ensure T is a list even when it has just one element
    r([H2|T2]) = r([a,b]);
    !assert_equals(a,H2);
    !assert_equals([b],T2);

    // Ensure T is an empty list
    r([H2_2|T2_2]) = r([a]);
    !assert_equals(a,H2_2);
    !assert_equals([],T2_2);

    // Get head and tail of an empty list should return false
    !assert_false(s([H3|T3]) = s([]));

    .log(warning,"TODO: Adding two rules statements are generating error. Uncomment line below and comment the statement member(Item, [Item|Tail]) on the top of this file.");
    //+{member(Item, [Item|Tail])};
    +{member(Item, [Head|Tail]) :- member(Item,Tail)};
    !assert_true(member(a, [a,b,c]));
    !assert_true(member(c, [a,b,c]));
    !assert_false(member(d, [a,b,c]));

    !test_list_in_context;

    !test_list_in_rules;
.

+!test_list_in_context :
    L = [a,b,c(0),1] & L = [H|T]
    <-
    !assert_equals(a,H);
    !assert_equals([b,c(0),1],T);
.

+!test_list_in_rules :
    another_rule(2,R)
    <-
    !assert_equals(3,R);
.
