/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

p(X,Y):-Y=X+1.

@[test]
+!test_list_rules
    <-
    +{n(X,Y):-Y=X-1};

    .log(warning,"TODO: Can't assert whether .list_rules is working properly");
    //.list_rules;
.
