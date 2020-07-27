/**
 * Test manager provides general test configurations and facilities
 */

/**
 * Configurations
 */
shutdown_hook.          // enable to shutdown after finishing tests

/**
 * Startup operations
 */
!setup_manager.          // starts test manager operations

/**
 * setup of the manager adding a hook for shutdown
 */
 @setup_manager[atomic]
+!setup_manager :
    .my_name(test_manager)
    <-
    .at("now +4 s", {+!shutdown_after_tests});
    .log(warning,"TODO: this hook is not safe, it must check if agents are running or if there are active intentions to shutdown only after that.");
    .log(info,"Set hook to shutdown");
.

/**
 * enable to shutdown after finishing tests
 */
 @shutdown_after_fail[atomic]
 +!shutdown_after_tests :
    shutdown_hook &
    .count(test_statistics(performed,_,_),N) &
    .count(test_statistics(failed,_,_),F) &
    .count(test_statistics(passed,_,_),P) &
    .count(plan_statistics(launched,_,_),LP) &
    .count(plan_statistics(achieved,_,_),AP) &
    F > 0
    <-
    .log(severe,"\n\n");
    .log(severe,"#",N," tests executed, #",P," passed and #",F," FAILED.");
    .log(severe,"End of Jason unit tests: FAILED!\n\n");
    for (test_statistics(R,T,A)) {
        .log(fine,"Agent '",A,"' ",R," test '",T,"'");
    }
    .stopMAS(0,1);
 .
@shutdown_after_skipped[atomic]
+!shutdown_after_tests :
    shutdown_hook &
    not intention(_) &
    .count(test_statistics(performed,_,_),N) &
    .count(test_statistics(failed,_,_),F) &
    .count(test_statistics(passed,_,_),P) &
    .count(plan_statistics(launched,_,_),LP) &
    .count(plan_statistics(achieved,_,_),AP) &
    LP \== AP
    <-
    .log(severe,"\n\n");
    .log(severe,"#",N," tests executed, #",P," passed and #",F," failed.");
    .log(severe,"#",LP," plans launched, but #",AP," achieved!");
    .log(severe,"Hook to shutdown FAILED! You may need to give more time for the shutdown.");
    .log(severe,"End of Jason unit tests: FAILED!\n\n");
    for (test_statistics(R,T,A)) {
        .log(fine,"Agent '",A,"' ",R," test '",T,"'");
    }
    .stopMAS(0,1);
.
@shutdown_after_success[atomic]
+!shutdown_after_tests :
    shutdown_hook &
    not intention(_) &
    .count(test_statistics(performed,_,_),N) &
    .count(test_statistics(failed,_,_),F) &
    .count(test_statistics(passed,_,_),P) &
    .count(plan_statistics(launched,_,_),LP) &
    .count(plan_statistics(achieved,_,_),AP)
    <-
    .log(info,"\n\n");
    .log(info,"#",N," tests executed, #",P," PASSED and #",F," failed.");
    .log(info,"End of Jason unit tests: PASSED\n\n");
    for (test_statistics(R,T,A)) {
        .log(fine,"Agent '",A,"' ",R," test '",T,"'");
    }
    .stopMAS;
.
+!shutdown_after_tests. // If auto shutdown is disabled

/**
 * create agents by files present in folder test/agt/
 */
@[atomic]
+!create_tester_agents(Path,Files) :
    .my_name(test_manager)
    <-
    .concat(Path,"/inc",PathInc);
    .list_files(PathInc,Files,IGNORE);
    .list_files(Path,Files,FILES);
    for (.member(M,FILES)) {
      if (not .nth(N,IGNORE,M)) {
        for (.substring("/",M,R)) {
          -+lastSlash(R);
        }
        ?lastSlash(R0);
        .length(M,L);
        .substring(M,AGENT,R0+1,L-4);
        .log(fine,"LAUNCHING: ",AGENT," (",M,")");
        .create_agent(AGENT,M);
      }
    }
.
+!create_tester_agents(_,_). // avoid plan not found for asl that includes controller

/**
 * Statistics for tests (passed/failed)
 */
@count_tests[atomic]
+!count_tests(R,T,A) // R \in [failed,passed]
    <-
    +test_statistics(performed,T,A);
    +test_statistics(R,T,A);
.

/**
 * Statistics for plans (a plan may have many tests/asserts)
 */
@count_plans[atomic]
+!count_plans(R,P,A) // R \in [launched,achieved]
    <-
    +plan_statistics(R,T,A);
.
