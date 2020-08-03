/**
 * Test manager provides general test configurations and facilities
 */

/**
 * Configurations
 */
shutdown_hook.          // shutdown after shutdown_delay(SD), SD is the number of seconds

/**
 * Startup operations
 */
!setup_manager.          // starts test manager operations

/**
 * setup of the manager adding a hook for shutdown
 */
 @setup_manager_user_delay[atomic]
+!setup_manager :
    shutdown_delay(SD)
    <-
    .concat("now +",SD," s",DD);
    .at(DD, {+!shutdown_after_tests});
    .log(info,"Set hook to shutdown");
.
@setup_manager_default_delay[atomic]
+!setup_manager
   <-
   .at("now +2 s", {+!shutdown_after_tests});
   .log(info,"Set hook to shutdown");
.

/**
 * enable to shutdown after finishing tests
 */
 @shutdown_after_fail[atomic]
 +!shutdown_after_tests :
    shutdown_hook &
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
    shutdown_hook &
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
    .log(severe,"#",LP," plans launched, #",AP," achieved, at least #",UP," not launched!");

    for (plan_statistics(launched,T,A) & not plan_statistics(achieved,T,A)) {
        .log(severe,"Test '",T,"' was NOT ACHIEVED, agent '",A,"' has FAILED on testing!");
    }
    .log(severe,"Hook to shutdown FAILED! You may need to set a higher shutdown_delay(SD).");
    .log(severe,"End of Jason unit tests: FAILED!\n\n");
    .stopMAS(0,1);
.
@shutdown_after_success[atomic]
+!shutdown_after_tests :
    shutdown_hook &
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
+!create_tester_agents(Path,Files) :
    .my_name(test_manager)
    <-
    .concat(Path,"/inc",PathInc);
    .list_files(PathInc,Files,IGNORE);
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

        .at("now +500 ms", {+!check_executing_test_plans(AGENT)});
    }
.
+!create_tester_agents(_,_). // avoid plan not found for asl that includes controller

/**
 * In case of a parser error, the agent will not be able to answer
 */
+!check_executing_test_plans(AGENT)
    <-
    .send(AGENT,askOne,executing_test_plans(_),executing_test_plans(AG));
    .term2string(AG,AGstr);
    AGENT = AGstr;
.
-!check_executing_test_plans(AGENT)
    <-
    !count_plans(unlaunched,execute_test_plans,AGENT);
    .log(severe,"check_executing_test_plans on agent ",AGENT," FAILED! It is likely to present parser errors.");
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
