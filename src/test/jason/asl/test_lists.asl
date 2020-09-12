/**
 * Test lists
 */

{ include("tester_agent.asl") }

member(Item, [Item|Tail]).
//member(Item, [Head|Tail]) :- member(Item,Tail).

/**
 * Test
 */
@[test]
+!test_lists
    <-
    .log(warning,"TODO: Jason does not support direct list unification? e.g.: [H|T] = [a,b,c(0),1]?")
    p([H|T]) = p([a,b,c(0),1]);
    !assert_equals(a,H);
    !assert_equals([b,c(0),1],T);

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
.
