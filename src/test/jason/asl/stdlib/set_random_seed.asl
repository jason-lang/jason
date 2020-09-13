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
    .set_random_seed(20);

    !assert_equals(7.320427537419555,math.random(10),0.01);
    .random(X,5)
    !assert_equals(0.7938483742301582,X,0.01);
    .random([1,2,3],Y)
    !assert_equals(2,Y,0.01);
    .random(Z)
    !assert_equals(0.6146790259334117,Z,0.01);
.
