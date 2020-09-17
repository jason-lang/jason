/**
 * Test plans for jason internal actions in stdlib:
 * Setting a random seed and then calling the random functions
 * (jason.functions.Random() / jason.stdlib.random())
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_set_random_seed
    <-
    .set_random_seed(20)
    !assert_equals(7.320,math.random(10),0.01)
    .random(X,5)
    !assert_equals(0.155,X,0.01)
    .random([1,2,3],Y)
    !assert_equals(3,Y)
    .random(Z)
    !assert_equals(0.453,Z,0.01);
.
