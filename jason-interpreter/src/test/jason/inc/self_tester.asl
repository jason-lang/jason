/**
 * self_tester is used to test Jason test library
 *
 * its statistics does not go to the manager, it is self
 * consumed
 */

{ include("$jasonJar/test/jason/inc/test_assert.asl") }

/**
 * Setup local statistics
 */
tests_performed(0).
tests_failed(0).
tests_passed(0).

/**
 * Startup operations
 */
!self_test_asserts.  // test all asserts both passed and failed conditions
!self_test_mock_agent. // test the mock agent

/**
 * -!P fail plan to generate assert failure
 * for others non expected errors
 */
@default_fail_plan[atomic]
-!P <-
    !force_failure("Failure captured by default fail plan -!P.");
.
+!create_default_fail_plan. // Do not create plans if it is disabled

/**
 * Send data to test_manager
 */
@[atomic]
+test(Test,passed,Src,Line)[An] :
    tests_performed(N) &
    tests_passed(P)
    <-
    -+tests_performed(N+1);
    -+tests_passed(P+1);
.
@[atomic]
+test(Test,failed,Src,Line)[An] :
    tests_performed(N) &
    tests_failed(F)
    <-
    -+tests_performed(N+1);
    -+tests_failed(F+1);
.

/**
 * Test all asserts, both passed and failed
 */
@[atomic]
+!self_test_asserts :
    true
    <-
    .log(severe,">>> starting self tests...");
    .log(severe,">>> notice it generates expected failures");
    .log(severe,">>> notice the results are not counted for general statistics");

    !assert_equals(1,1);    //should pass
    !assert_equals(1,2);    //should fail

    !assert_equals(2,2.0000000000);    //should pass
    !assert_equals(2,2.0000000001);    //should fail

    !assert_equals([1,2],[1,2]);    //should pass
    !assert_equals([1,2],[1,2,3]);  //should fail

    !assert_equals(1,1,0);  //should pass
    !assert_equals(1,2,0);  //should fail

    +this_is_a_belief;
    !assert_true(this_is_a_belief);     //should pass
    !assert_true(this_is_NOT_a_belief); //should fail

    !assert_false(this_is_NOT_a_belief);    //should pass
    !assert_false(this_is_a_belief);        //should fail

    !force_pass;            //should pass
    !force_failure(msg);    //should fail

    !assert_contains([1,2],1);  //should pass
    !assert_contains([1,2],3);  //should fail

    !assert_greater_than(1.01,1);  //should pass
    !assert_greater_than(-1.01,1);  //should fail

    !assert_greater_than_equals(1,1);  //should pass
    !assert_greater_than_equals(-1,1);  //should fail

    !assert_between(1,0.99,1);  //should pass
    !assert_between(1,1,1.01);  //should pass
    !assert_between(1,1,1);  //should pass
    !assert_between(0.99,1,1);  //should fail
    !assert_between(1.01,1,1);  //should fail
    !assert_between(1.0,-1,0);  //should fail

    .log(severe,">>> asserting if self tests went right");
    .log(severe,">>> notice that no fails are expected from now on");

    // getting stats from tests above (the next asserts are not being taken into account)
    ?tests_performed(N);
    ?tests_passed(P);
    ?tests_failed(F);

    // check if everything expected pass and fails have occurred
    !assert_equals(26,N);
    !assert_equals(13,P);
    !assert_equals(13,F);

    .log(severe,">>> end of self tests.");
.

/**
 * Test if the mock agent is sending its sleeping state
 */
@[atomic]
+!self_test_mock_agent :
    .my_name(ME)
    <-
    !assert_false(sleeping(mock));

    .create_agent(mock, "mock_agent.asl");
    .send(mock, tell, mock_owner(ME));

    .wait( not sleeping(mock), 300, _ );
    .wait( sleeping(mock) );

    !assert_true(sleeping(mock));
    .kill_agent(mock);
.
