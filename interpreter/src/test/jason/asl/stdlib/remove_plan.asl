/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_remove_plan
    <-
    .add_plan({ @l1 +!p : true <- +p1 });
    .relevant_plans({+!p},LP1);
    .length(LP1,LLP1);
    !assert_equals(1,LLP1);

    .remove_plan(l1);
    .relevant_plans({+!p},LP2);
    .length(LP2,LLP2);
    !assert_equals(0,LLP2);

    .add_plan({ @l1_1 +!p : true <- +p1_1 }, bob);
    .add_plan({ @l1_2 +!p : true <- +p1_2 });
    .relevant_plans({+!p},LP1_1);
    .length(LP1_1,LLP1_1);
    !assert_equals(2,LLP1_1);
    .remove_plan(l1_1,bob);
    .remove_plan(l1_2,bob); //Should not take effect
    .relevant_plans({+!p},LP1_2);
    .length(LP1_2,LLP1_2);
    !assert_equals(1,LLP1_2);
    .remove_plan(l1_2);

    .add_plan({ @l1_1 +!p : true <- +p1_1 }, bob);
    .add_plan({ @l1_2 +!p : true <- +p1_2 });
    .add_plan({ @l1_3 +!p : true <- +p1_3 }, bob);
    .relevant_plans({+!p},LP1_3);
    .length(LP1_3,LLP1_3);
    !assert_equals(3,LLP1_3);
    .remove_plan([l1_1,l1_2,l1_3],bob); //Should not remove l1_2
    .relevant_plans({+!p},LP1_4);
    .length(LP1_4,LLP1_4);
    !assert_equals(1,LLP1_4);
    .remove_plan(l1_2);

    .add_plan({ @l1 +!p : true <- +p1 });
    .add_plan({ @l2 +!p : true <- +p2 });
    .add_plan({ @l3 +!p : true <- +p3 });
    .add_plan({ @l4 +!p : true <- +p4 });
    .relevant_plans({+!p},LP3);
    .length(LP3,LLP3);
    !assert_equals(4,LLP3);
    .remove_plan([l1,l2,l3]);
    .relevant_plans({+!p},LP4);
    .length(LP4,LLP4);
    !assert_equals(1,LLP4);

    .relevant_plans({ +!p },_,LP5);
    .remove_plan(LP5);
    .relevant_plans({ +!p },_,LP6);
    .length(LP6,LLP6);
    !assert_equals(0,LLP6);
.
