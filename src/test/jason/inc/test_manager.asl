/**
 * Test manager provides general test configurations and facilities
 */

/**
 * Configurations
 */
shutdown_hook.          // shutdown after finishing or shutdown_delay(SD). Default: disabled

/**
 * Startup operations
 */
!setup_manager.          // starts test manager operations

/**
 * setup of the manager adding a hook for shutdown
 */
+!setup_manager <-
    if (shutdown_hook) { //If the hook for timeout is enabled
        if (shutdown_delay(SD)) { //SD is the number of seconds
            .concat("now +",SD," s",DD);
            .at(DD, {+!set_timeout});
        } else {
            .at("now +600 s", {+!set_timeout});
        }
    }
    .at("now +2 s", {+!shutdown_when_done});
.

/**
 * Inform that timeout for shutdown is up
 */
+!set_timeout
    <-
    +time_is_up;
.

/**
 * enable to shutdown after finishing tests
 */
+!shutdown_when_done :
    .count(launching_test_set(_),ETS) &
    .count(launched_test_set(_),LTS) &
    ETS == LTS & // All tester_agents have launched their test sets
    .count(plan_statistics(launched,_,_),LP) &
    .count(plan_statistics(achieved,_,_),AP) &
    LP == AP  //All LAUNCHED plans were ACHIEVED
    <-
    !shutdown_after_tests;
.
+!shutdown_when_done :
    shutdown_hook &
    time_is_up
    <-
    !shutdown_after_tests;
.
+!shutdown_when_done
    <-
    .wait(1000);
    !shutdown_when_done;
.

/**
 * enable to shutdown after finishing tests
 */
 @shutdown_after_fail[atomic]
 +!shutdown_after_tests :
    .count(test_statistics(failed,_,_),F) &
    F > 0 &
    .count(test_statistics(performed,_,_),N) &
    .count(test_statistics(passed,_,_),P)
    <-
    .log(severe,"\n\n");
    .log(severe,"#",N," tests executed, #",P," passed and #",F," FAILED.");
    .log(severe,"End of Jason unit tests: FAILED!\n\n");
    .stopMAS(0,1);
 .
@shutdown_after_skipped[atomic]
+!shutdown_after_tests :
    not intention(_) &
    .count(test_statistics(performed,_,_),N) &
    .count(test_statistics(failed,_,_),F) &
    .count(test_statistics(passed,_,_),P) &
    .count(plan_statistics(unlaunched,_,_),UP) &
    .count(plan_statistics(launched,_,_),LP) &
    .count(plan_statistics(achieved,_,_),AP) &
    (LP \== AP | UP > 0)
    <-
    .log(severe,"\n\n");
    .log(severe,"#",N," tests executed, #",P," passed and #",F," failed.");
    if (LP \== AP) {
        .log(severe,"#",LP," plans launched but only #",AP," achieved!");
    }
    if (UP > 0) {
        .log(severe,"At least #",UP," plan(s) not launched!");
    }

    for (plan_statistics(launched,T,A) & not plan_statistics(achieved,T,A)) {
        .log(severe,"Test '",T,"' was NOT ACHIEVED, agent '",A,"' has FAILED on testing!");
    }
    .log(severe,"Hook to shutdown FAILED! You may need to set a higher shutdown_delay(SD).");
    .log(severe,"End of Jason unit tests: FAILED!\n\n");
    .stopMAS(0,1);
.
@shutdown_after_success[atomic]
+!shutdown_after_tests :
    not intention(_) &
    .count(test_statistics(performed,_,_),N) &
    .count(test_statistics(failed,_,_),F) &
    .count(test_statistics(passed,_,_),P)
    <-
    .log(info,"\n\n");
    .log(info,"#",N," tests executed, #",P," PASSED and #",F," failed.");
    .log(info,"End of Jason unit tests: PASSED\n\n");
    .stopMAS;
.
+!shutdown_after_tests // If there is an active intention
    <-
    .log(info,"waiting to finish active intention...");
    .wait(1000);
    !shutdown_after_tests;
.

/**
 * create agents by files present in folder test/agt/
 */
+!create_tester_agents(Path,Files)
    <-
    !create_tester_agents(Path,Files,"");
.
+!create_tester_agents(Path,Files,IgnoreSubFolder) :
    .my_name(test_manager)
    <-
    if (.length(IgnoreSubFolder) > 0) {
        .concat(Path,IgnoreSubFolder,PathInc);
        .list_files(PathInc,Files,IGNORE);
    } else {
        IGNORE = [];
    }
    .list_files(Path,Files,FILES);
    for (.member(M,FILES) & not .nth(N,IGNORE,M)) {
        for (.substring("/",M,R)) {
            -+lastSlash(R);
        }
        ?lastSlash(R0);
        .length(M,L);
        .substring(M,AGENT,R0+1,L-4);
        .log(fine,"LAUNCHING: ",AGENT," (",M,")");
        .create_agent(AGENT,M);

        // The tool is expecting that all agents in this folder are tester_agents
        .at("now +500 ms", {+!check_launching_test_set(AGENT)});
    }
.
-!create_tester_agents(_,_,_)
    <-
    +fatal_error;
.

/**
 * In case of a parser error, the agent won't say it is launching_test_set
 */
+!check_launching_test_set(AGENT) :
    .term2string(AG,AGENT) &
    launching_test_set(AG).
-!check_launching_test_set(AGENT)
    <-
    !count_plans(unlaunched,execute_test_plans,AGENT);
    .log(severe,"check_launching_test_set on agent ",AGENT," FAILED! Please, check if all agents in the src/test folder are tester_agent and check for parser errors.");
.

/**
 * Statistics for tests (passed/failed)
 */
@count_tests[atomic]
+!count_tests(Status,Test,Agent) // R \in [performed,failed,passed]
    <-
    +test_statistics(performed,Test,Agent);
    +test_statistics(Status,Test,Agent);
.

/**
 * Statistics for plans (a plan may have many tests/asserts)
 */
@count_plans[atomic]
+!count_plans(Status,Plan,Agent) // R \in [launched,achieved,unlaunched]
    <-
    +plan_statistics(Status,Plan,Agent);
.

/**
 * Fatal error informed
 */
 @fatal_error
 +fatal_error
    <-
    .log(severe,"\n\n");
    .log(severe,"Fatal error informed. It is likely to be an error on code/parser!");
    .log(severe,"No test statistics will be displayed. FAILED!\n\n");
    .stopMAS(0,1);
 .
