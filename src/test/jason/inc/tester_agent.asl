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
 * Startup operations
 */
!execute_test_plans.    // automatically launch all plans with [test] annotation

/**
 * execute plans that contains "test" in the label
 */
+!execute_test_plans:
    .relevant_plans({+!_},LP,LL) &
    .my_name(ME)
    <-
    !create_default_fail_plan;

    .send(test_manager,tell,launching_test_set(ME));
    for (.member(Label[test],LL)) {
        .findall(T, .member(P,LP) & P = {@L +!T : C <- B} & Label = L, Plans);
        .member(Plan,Plans); // it is expected only one plan in the list

        .send(test_manager,achieve,count_plans(launched,Plan,ME));
        /**
         * Execute the test plan
         */
        !execute_test_plan(Plan);
    }
    .send(test_manager,tell,launched_test_set(ME));
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
        -!P[error(E),code(C),code_src(S),code_line(L),error_msg(M)] :
            true
            <-
            .log(severe,"Error '",E,"' in '",S,"' on event '",C,"' at line ",L," FAILED! Message: '",M);
            .send(test_manager,tell,fatal_error);
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
 * Send data to test_manager the results of each assertion
 */
@[atomic]
+test(Test,Result,Src,Line)[source(self)] :
    .my_name(ME)
    <-
    .send(test_manager,achieve,count_tests(Result,Test,ME));
.
@[atomic]
+test(Test,Result,Src,Line)[source(Agent)]
    <-
    .send(test_manager,achieve,count_tests(Result,Test,Agent));
.
/**
 * Use meta event to tell to the manager goal a plan was achieved
 */
@[atomic]
^!P[state(achieved)] :
    .relevant_plans({+!P},_,L) & // Get all plan labels
    .member(I[test],L) & // From the list of plan labels, get the test ones
    .my_name(ME)
    <-
    .send(test_manager,achieve,count_plans(achieved,P,ME));
.

/**
 * Create a mock agent able to tell to the owner when it
 * is sleeping which can be used to make sure the agent
 * is ready to answer properly for tests
 */
+!create_mock_agent(MockAgName) :
    .my_name(ME)
    <-
    .create_agent(MockAgName, "mock_agent.asl");
    .send(MockAgName, tell, mock_owner(ME));
.
/**
 * Wait for the mock agent to be in idle mode, i.e., ready
 * to do something.
 */
+!wait_idle(MockAgName)
    <-
    // if the mock was already sleeping, wait it wake up first
    .wait( not sleeping(MockAgName), 300, _ );
    // wait the mock finish some task and tell that it is now sleeping
    .wait( sleeping(MockAgName) );
.
