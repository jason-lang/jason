/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_relevant_rules
    <-
    .relevant_rules(p(_,_),LP0);
    // Antigo
    // .length(LP0, L0)
    // Novo
    .length(LP0, L0);
    !assert_equals(0, L0);

    +{p(X,Y):-Y=X+1};

    .relevant_rules(p(_,_),LP1);
    // Antigo
    // .length(LP1, L1)
    // Novo
    .length(LP1, L1);
    !assert_equals(1, L1);

    ?p(10,Y1);
    !assert_equals(11, Y1);
.
