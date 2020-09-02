/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_remove_plan
    <-
    .add_plan({ @goto_when_raining +!goto(X,Y) : raining <- +carrying_umbrella; +arrived });
    .add_plan({ @goto_other_conditions +!goto(X,Y) <- +arrived });
    .add_plan({ @another_plan +!ap <- +ap1 });
    .relevant_plans({+!goto(X,Y)},LP_1);
    .length(LP_1,LLP_1);
    !assert_equals(2,LLP_1);

    .relevant_plans({+!goto(X,Y)},LP_2,LL_2);
    !assert_contains(LL_2,goto_when_raining[source(self)]);
    !assert_contains(LL_2,goto_other_conditions[source(self)]);
    !assert_not_contains(LL_2,another_plan[source(self)]);

    .relevant_plans({+!_},_,LL_3);
    !assert_contains(LL_3,goto_when_raining[source(self)]);
    !assert_contains(LL_3,goto_other_conditions[source(self)]);
    !assert_contains(LL_3,another_plan[source(self)]);
.
