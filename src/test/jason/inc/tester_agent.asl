/**
 * Tester agent is an agent managed by test_manager
 */

{ include("$jasonJar/test/jason/inc/test_assert.asl") }
{ include("$jasonJar/test/jason/inc/test_performance.asl") }

/**
 * Configurations
 */
auto_create_fail_plan.  // create -!P fail plan to capture unexpected failures

/**
 * execute plans that contains "test" in the label
 */
+!execute_test_plans:
    .relevant_plans({+!_},LP,LL) &
    .my_name(ME)
    <-
    !create_default_fail_plan;

    for (.member(Label[test],LL)) {
        .findall(T, .member(P,LP) & P = {@L +!T : C <- B} & Label = L, Plans);
        .member(Plan,Plans); // it is expected only one plan in the list

        .send(test_manager,achieve,count_plans(launched,Plan,ME));
        /**
         * Execute the test plan
         */
        !execute_test_plan(Plan);
    }
.

/**
 * Add a default -!P fail plan to generate
 * assert failure for others non expected
 * errors
 */
+!create_default_fail_plan:
    auto_create_fail_plan
    <-
    .add_plan({
        -!P[code(C),code_src(S),code_line(L),error_msg(M)] :
            true
            <-
            .log(severe,
                "The event '",C,"' on '",S,"' at line ",L," FAILED! Message: '",M,"' Error on code/parser! ",
                "No test statistics will be displayed.");
            .stopMAS(0,1);
    }, self, end);
.
+!create_default_fail_plan. // Do not create plans if it is disabled

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
+test(Test,Result,Src,Line)[source(Agent)] :
    .my_name(ME)
    <-
    if (Agent == self) {
        .send(test_manager,achieve,count_tests(Result,Test,ME));
    } else {
        .send(test_manager,achieve,count_tests(Result,Test,Agent));
    }
.

^!P[state(STATE)] :
    .relevant_plans({+!P},_,L) & // Get all plan labels
    .member(I[test],L) & // From the list of plan labels, get the ones
    STATE == achieved &
    .my_name(ME)
    <-
    .send(test_manager,achieve,count_plans(achieved,P,ME));
.
