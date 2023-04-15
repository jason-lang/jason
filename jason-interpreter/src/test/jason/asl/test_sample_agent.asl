/**
 * Sample agent to show how to use jason unit tests
 * This agent contains plans to execute some actions and
 * test plans to test these plan.
 *
 * Another way is to add here only test plans including
 * the agent to be tested like this: { include("sampleAgent.asl") }
 *
 * To execute automatically test plans, they should have an
 * annotation test in the label (e.g.: @my_label[test],
 * @[atomic,test]).
 */

{ include("tester_agent.asl") }

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



/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                    A G E N T    P L A N S
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * A sample plan in which it is expected to generate a belief
 */
+!do_something_adding_belief :
    true
    <-
    ?sum(1,2,R);
    +raining;
.

/**
 * Add to belief string that starts with 'test'
 */
+!add_to_belief_test :
    true
    <-
    for (.member(S,["test_k","hey_test_bla","test_j"])) {
      if (.substring("test",S,0)) {
        +starts_with_h(S);
      }
    }
.

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                    T E S T     P L A N S
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Test sum using just equals(EXPECTED,ACTUAL)
 */
@[atomic,test]
+!test_sum :
    true
    <-
    ?sum(1.3,2.6,R);
    !assert_equals(3.9,R,0.1);
.

/**
 * Test div using equals(EXPECTED,ACTUAL,TOLERANCE)
 */
@[atomic,test]
+!test_divide :
    true
    <-
    ?divide(10,3,R);
    !assert_equals(3.33,R,0.01);
.

/*
 * Test if the agent has added a belief
 */
@[atomic,test]
+!test_do_something_adding_belief :
    true
    <-
    .abolish(raining);
    !assert_false(raining);
    !do_something_adding_belief;
    !assert_true(raining);
.

/**
 * Test if substring(_,_,0) really works as java startsWith
 */
@[atomic,test]
+!test_add_to_belief_test :
    true
    <-
    .abolish(starts_with_h(_));
    !assert_false(starts_with_h(_));
    !add_to_belief_test;
    !assert_true(starts_with_h("test_k"));
    !assert_false(starts_with_h("hey_test_bla"));
    !assert_true(starts_with_h("test_j"));
.
