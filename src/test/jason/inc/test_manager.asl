/**
 * Test manager provides general test configurations and facilities
 */

/**
 * Setup statistics
 */
tests_performed(0).
tests_failed(0).
tests_passed(0).

/**
 * Configurations
 */
shutdown_hook.          // enable to shutdown after finishing tests

/**
 * Startup operations
 */
!set_controller.          // starts test controller operations

/**
 * setup of the controller, including hook for shutdown
 */
 @set_controller[atomic]
+!set_controller :
    .my_name(test_manager)
    <-
    .at("now +2 s", {+!shutdown_after_tests});
    .log(info,"Set hook to shutdown");
.
+!set_controller. // avoid plan not found for asl that includes controller

/**
 * enable to shutdown after finishing tests
 */
 @shutdown_after_fail[atomic]
 +!shutdown_after_tests :
     shutdown_hook &
     failed &
     tests_performed(N) &
     tests_failed(F) &
     tests_passed(P)
     <-
     .log(severe,"\n\n");
     .log(severe,"#",N," tests executed, #",P," passed and #",F," FAILED.");
     .log(severe,"End of Jason unit tests: FAILED!\n\n");
     .stopMAS(0,1);
 .
@shutdown_after_success[atomic]
+!shutdown_after_tests :
    shutdown_hook &
    not intention(_) &
    tests_performed(N) &
    tests_failed(F) &
    tests_passed(P)
    <-
    .log(info,"\n\n");
    .log(info,"#",N," tests executed, #",P," PASSED and #",F," failed.");
    .log(info,"End of Jason unit tests: PASSED\n\n");
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
@count_tests_passed[atomic]
+!count_tests(passed) :
    tests_performed(N) &
    tests_passed(P)
    <-
    -+tests_performed(N+1);
    -+tests_passed(P+1);
.
@count_tests_failed[atomic]
+!count_tests(failed) :
    tests_performed(N) &
    tests_failed(F)
    <-
    +failed;
    -+tests_performed(N+1);
    -+tests_failed(F+1);
.
