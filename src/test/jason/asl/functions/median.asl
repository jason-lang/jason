/**
 * Test plans for jason functions
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_median
    <-
    !assert_equals(2, math.median([1,3]));
    !assert_equals(1, math.median([1,1,3]));

    !test_median_non_numeric;
    .wait(100); // Give some time to answer to get the meta event result
    !assert_true(state(test_median_non_numeric,failed));

    !test_median_empty_set;
    .wait(100); // Give some time to answer to get the meta event result
    !assert_true(state(test_median_empty_set,failed));
.

+!test_median_non_numeric
    <-
    .log(warning,"TODO: Although there is a -! plan, log is still printing many error messages");
    math.median([1,1,3,a]);
.
-!test_median_non_numeric.
^!test_median_non_numeric[state(S)]
    <-
    +state(test_median_non_numeric,S);
    .log(warning,state(S));
.

+!test_median_empty_set
    <-
    math.median([]);
.
-!test_median_empty_set.
^!test_median_empty_set[state(S)]
    <-
    +state(test_median_empty_set,S);
    .log(warning,state(S));
.
