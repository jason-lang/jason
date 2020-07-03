/**
 * Test manager provides general test configurations and facilities
 */

/**
 * Configurations
 */
verbose.        // enable to see full log debug
shutdown_hook.     // enable to shutdown after finishing tests

/**
 * Startup operations
 */
!set_controller.    // starts test controller operations
!create_test_agents.     // create agents by .asl files in test/agt/

/**
 * execute plans that contains "test" in the name
 */
@execute_plans[atomic]
+!execute_test_plans:
    .relevant_plans({+!_},_,LL)
    <-
    for (.member(P,LL)) {
      if (.substring("test",P,0)) {
        !!execute_test_plan(P);
      }
    }
.

@execute_plan[atomic]
+!execute_test_plan(P) :
    true
    <-
    .current_intention(I);
    I = intention(Id,IStack);
    .print("TESTING ",Id," (main plan: ",P,")");
    !P;
.

/**
 * setup of the controller, including hook for shutdown
 */
 @set_controller[atomic]
+!set_controller :
    .my_name(test_manager)
    <-
    .print("\n\n");
    .print("**** Starting Jason unit tests...\n\n");

    .at("now +2 s", {+!shutdown_after_tests});
.
+!set_controller. // avoid plan not found for asl that includes controller

/**
 * enable to shutdown after finishing tests
 */
 @shutdown_after_error[atomic]
 +!shutdown_after_tests :
     shutdown_hook &
     error
     <-
     .print("\n\n");
     .print("**** End of Jason unit tests: FAILED!\n\n");
     .exit_error;
 .
@shutdown_after_success[atomic]
+!shutdown_after_tests :
    shutdown_hook &
    not intention(_)
    <-
    .print("\n\n");
    .print("**** End of Jason unit tests: PASSED\n\n");
    .stopMAS;
.
+!shutdown_after_tests. // If auto shutdown is disabled

/**
 * create agents by files present in folder test/agt/
 */
@create_agents[atomic]
+!create_test_agents :
    .my_name(test_manager)
    <-
    .list_files("./src/test/jason/inc",".*.asl",IGNORE);
    .list_files("./src/test/jason/",".*.asl",FILES);
    for (.member(M,FILES)) {
      if (not .nth(N,IGNORE,M)) {
        for (.substring("/",M,R)) {
          -+lastSlash(R);
        }
        ?lastSlash(R0);
        .length(M,L);
        .substring(M,AGENT,R0+1,L-4);
        .print("Launching : ",AGENT," (",M,")");
        .create_agent(AGENT,M);
      }
    }
.
+!create_test_agents. // avoid plan not found for asl that includes controller
