/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_plan_label
    <-
    .add_plan({ @l1 +!p1 : true <- +p1 }, bob);
    .plan_label(PB1,l1);
    !assert_equals(PB1,{ @l1[source(bob)] +!p1 <- +p1 });

    .add_plan({ @l2 +!p2 : true <- +p2 });
    .plan_label({@l2[source(self)] +!p2 <- +p2},PL2_1);
    !assert_equals(l2[source(self)],PL2_1);

    // Using another plan format
    .plan_label({ @l2[source(self)] +!p2 : true <- +p2},PL2_2);
    !assert_equals(l2[source(self)],PL2_2);

    // When the label is really unknown
    .plan_label({ @_ +!p2 : true <- +p2},PL2_3);
    //!assert_equals(l2[source(self)],PL2_3);
    .log(warning,"TODO: It is expected a way to get an unknown label of a known plan, no?");
.
