/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_list_plans
    <-
    .add_plan({ @l1_1 +!g(X) : true <- +g1(X) }, bob);
    .add_plan({ @l1_2 +!p : true <- +p1 });

    .log(warning,"TODO: Can't assert whether .list_plans is working properly");
    //.list_plans;
    //.list_plans({ +g(_) });
.
