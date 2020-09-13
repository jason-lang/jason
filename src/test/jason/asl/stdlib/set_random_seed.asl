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

    !assert_equals(7.320427537419555,math.random(10));
    .random(X,5)
    !assert_equals(0.7938483742301582,X);
    .random(Y,1,2)
    !assert_equals(0.1630765589278791,Y);
    .random(Z)
    !assert_equals(0.15501780258230335,Z);
.
