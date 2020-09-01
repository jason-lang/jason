/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_add_plan
    <-
    !assert_false(b1);
    .add_plan({ +b : true <- +b1 });
    +b;
    !assert_true(b1);

    !assert_false(c1);
    .add_plan("+c : true <- +c1.");
    +c;
    !assert_true(c1);

    !assert_false(d1);
    .add_plan({ +!d : true <- +d1 }, rafa);
    !d;
    !assert_true(d1);

    !assert_false(d2);
    .add_plan({ +!d : true <- +d2 }, rafa, begin);
    !d;
    !assert_true(d2);

    !assert_false(e1);
    !assert_false(f1);
    .add_plan([{+!e <- +e1}, {+f : true <- +f1}], rafa);
    !e;
    +f;
    !assert_true(e1);
    !assert_true(f1);
    .log(warning,"TODO: Assert if the source of the plan is rafa.");
.
