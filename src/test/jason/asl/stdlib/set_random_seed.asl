/**
 * Test plans for jason internal actions in stdlib:
 * Setting a random seed and then calling the random functions
 * (jason.functions.Random() / jason.stdlib.random())
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test,atomic]
+!test_set_random_seed
    <-
    .set_random_seed(20);
    RET = math.random(10);
    !assert_equals(7.320,RET,0.01);
    //.random(X,5);
    .random(X);
    !assert_equals(0.206,X,0.01);
    .log(warning,"TODO: Use of lists in .random seems to be producing undeterministic responses.");
    //.random([1,2,3],Y);
    //!assert_equals(3,Y);
    .random(Y);
    !assert_equals(0.793,Y,0.01);
    .random(Z);
    !assert_equals(0.163,Z,0.01);
.
