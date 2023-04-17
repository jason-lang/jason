/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_random
    <-
    .random(X);//: unifies X with one random number between 0 and 1.
    !assert_equals(0.5,X,0.5);  // The returned value is between 0 and 1, i.e., 0.5 +/- 0.5

    .findall(X2, .random(X2,5), L2);
    !assert_equals(5,.length(L2));
    for (.member(XX,L2)) {
        !assert_equals(0.5,XX,0.5);  // The returned value is between 0 and 1, i.e., 0.5 +/- 0.5
    }

    if (.random(X3, 0) & X3 > 0.7 & X3 < 0.8) { //: unifies X with a random number between 0 and 1, and backtracks infinitely.
        !assert_equals(0.75,X3,0.05);  // The returned value is between 0.7 and 0.8
        +random_infinitely(X3);
    }
    !assert_true(random_infinitely(_));

    .random([a,b,c],X4);//: unifies X with a random value from a, b, or c.
    !assert_contains([a,b,c],X4);

    /**
     * It is supposed to be a normal distribution with mean around 0.5, using std_dev
     * as tolerance is not scientific, is just a very safe way to avoid failure on this test
     */
    .findall(X5, .random(X5,50), L5);
    !assert_equals(50,.length(L5));
    MEAN = math.mean(L5);
    MEDIAN = math.median(L5);
    STD = math.std_dev(L5);
    !assert_equals(0.5,MEAN,STD);
    !assert_equals(0.5,MEDIAN,STD); // It is supposed to be a normal distribution with mean around 0.5
.
