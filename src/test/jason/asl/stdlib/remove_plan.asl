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

    .log(warning,"TODO: Assert if removed plan had bob as source.");
    //.remove_plan(l1,bob): removes the plan identified by label l1[source(bob)]. Note that a plan with a source like that was probably added to the plan library by a tellHow message.

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

    .log(warning,"TODO: Assert if removed plans had bob as source.");
    //.remove_plan([l1,l2,l3],bob): removes the plans identified by labels l1[source(bob)], l2[source(bob)], and l3[source(bob)].

    .relevant_plans({ +!p },_,LP5);
    .remove_plan(LP5);
    .relevant_plans({ +!p },_,LP6);
    .length(LP6,LLP6);
    !assert_equals(0,LLP6);
.
