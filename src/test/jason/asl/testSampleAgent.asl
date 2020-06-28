/**
 * Sample agent to show how to use jason unit tests
 * This agent contains plans to execute some actions and
 * test plans to test these plan.
 *
 * Another way is to add here only test plans including
 * the agent to be tested like this: { include("sampleAgent.asl") }
 *
 * To execute automatically test plans, they should have a label
 * starting with @test (e.g.: @testSum[atomic])
 */

{ include("testAssert.asl") }

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     A G E N T    I N I T I A L    B B    A N D    R U L E S
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * A rule to calculate the sum of two numbers
 */
sum(X,Y,R):-R = X + Y.

/**
 * A rule to calculate the division of two numbers
 */
divide(X,Y,R):-R = (X / Y).

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        A G E N T     I N I T I A L    D E S I R E S
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

!executeTestPlans.


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                    A G E N T    P L A N S
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * A sample plan in which it is expected to generate a belief
 */
+doSomethingAddsBelief :
    true
    <-
    ?sum(1,2,R);
    +raining;
.


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                    T E S T     P L A N S
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Test sum using just equals(EXPECTED,ACTUAL)
 */
@testSum[atomic]
+!testSum :
    true
    <-
    ?sum(1.3,2.6,R);
    !assertEquals(3.95,R);
.

/**
 * Test div using equals(EXPECTED,ACTUAL,TOLERANCE)
 */
@testDivide[atomic]
+!testDivide :
    true
    <-
    ?divide(10,3,R);
    !assertEquals(3.33,R,0.01);
.

/*
 * Test if the agent has added a belief
 */
@testDoSomethingAddsBelief[atomic]
+!testDoSomethingAddsBelief :
    true
    <-
    .abolish(raining);
    !assertFalse(raining);
    +doSomethingAddsBelief;
    !assertTrue(raining);
.
