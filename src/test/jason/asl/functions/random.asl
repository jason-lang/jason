/**
 * Test plans for jason functions
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_random
    <-
    !assert_equals(0.5, math.floor(math.random), 0.5); // The returned value is between 0 and 1, i.e., 0.5 +/- 0.5

    !assert_equals(5, math.floor(math.random(11)), 5); // The returned value is between 0 and 10, i.e., 5 +/- 5
.
