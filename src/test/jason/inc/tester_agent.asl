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
    .relevant_plans({+!_},LP,LL)
    <-
    !create_default_fail_plan;

    for (.member(Label,LL)) {
        Label = Name[Head|Tail];

        if ((Head == test) | (.list(Tail) & (.member(test,Tail)))) {

            .findall(T, .member(P,LP) & P = {@L +!T : C <- B} & Label = L, Plans);
            .member(Plan,Plans); // it is expected only one plan in the list

            /**
             * Execute the test plan
             */
            !!execute_test_plan(Plan);
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
