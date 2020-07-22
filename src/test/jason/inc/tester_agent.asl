/**
 * Tester agent is an agent managed by test_manager
 */

{ include("test_assert.asl") }

/**
 * Configurations
 */
auto_create_fail_plan.  // create -!P fail plan to capture unexpected failures

/**
 * Startup operations
 */
//!self_test_asserts.  // tests all asserts both passed and failed conditions

/**
 * execute plans that contains "test" in the label
 */
@execute_plans[atomic]
+!execute_test_plans:
    .relevant_plans({+!_},_,LL)
    <-
    !create_default_fail_plan;

    for (.member(P,LL)) {
        if (.substring("test",P,0)) {
            /**
             * Execute the @test plan
             */
            !!execute_test_plan(P);
        }
    }
.

/**
 * Add a default -!P fail plan to generate
 * assert failure for others non expected
 * errors
 */
@create_default_fail_plan[atomic]
+!create_default_fail_plan:
    auto_create_fail_plan
    <-
    .add_plan({
        -!P <-
            !force_failure("Failure captured by default fail plan -!P.");
    }, self, end);
.
+!create_default_fail_plan. // Do not create plans if it is disabled

@execute_plan[atomic]
+!execute_test_plan(P) :
    .intention(ID,_,_,current)
    <-
    .log(fine,"TESTING ",ID," (main plan: ",P,")");
    !P;
.

/**
 * Send data to test_manager
 */
@test_counter[atomic]
+test(Test,Result,Src,Line)[An] :
    true
    <-
    .send(test_manager,achieve,count_tests(Result));
.

/**
 * Test all asserts, both passed and failed
 */
@self_test_asserts[atomic]
+!self_test_asserts :
    true
    <-
    !assert_equals(1,1);
    !assert_equals(1,2);

    !assert_equals([1,2],[1,2]);
    !assert_equals([1,2],[1,2,3]);

    !assert_equals(1,1,0);
    !assert_equals(1,2,0);

    +this_is_a_belief
    !assert_true(this_is_a_belief);
    !assert_true(this_is_NOT_a_belief);

    !assert_false(this_is_NOT_a_belief);
    !assert_false(this_is_a_belief);

    !force_pass;
    !force_failure(msg);

    !assert_contains([1,2],1);
    !assert_contains([1,2],3);
.
