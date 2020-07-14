/**
 * Tester agent is an agent managed by test_manager
 */

{ include("test_assert.asl") }

/**
 * Configurations
 */
auto_create_fail_plan.  // create -!P fail plan to capture unexpected failures

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
    .log(info,"TESTING ",ID," (main plan: ",P,")");
    !P;
.

/**
 * Send data to test_manager
 */
@test_passed[atomic]
+test_passed :
    true
    <-
    .send(test_manager,achieve,count_tests(passed));
.

@test_failed[atomic]
+test_failed :
    true
    <-
    .send(test_manager,achieve,count_tests(failed));
.
