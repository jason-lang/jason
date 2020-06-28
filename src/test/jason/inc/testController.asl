/**
 * Test controller provides general test configurations and facilities
 */

/**
 * Configurations
 */
verbose.        // enable to see full log debug
shutdownHook.     // enable to shutdown after finishing tests

/**
 * Startup operations
 */
!setTestController.    // starts test controller operations
!createTestAgents.     // create agents by .asl files in test/agt/

/**
 * execute plans that contains "test" in the name
 */
@executeTestPlans[atomic]
+!executeTestPlans:
    .relevant_plans({+!_},_,LL)
    <-
    for (.member(P,LL)) {
      if (.substring("test",P)) {
        !!executeTestPlan(P);
      }
    }
.

@executeTestPlan[atomic]
+!executeTestPlan(P) :
    true
    <-
    .current_intention(I);
    I = intention(Id,IStack);
    .print("Executing ",Id," (main plan: ",P,")");
    !P;
.

/**
 * setup of the controller, including hook for shutdown
 */
 @setTestController[atomic]
+!setTestController :
    .my_name(testController)
    <-
    .print("\n\n");
    .print("**** Starting Jason unit tests...\n\n");

    .at("now +2 s", {+!shutdownAferTests});
.
+!setTestController. // avoid plan not found for asl that includes controller

/**
 * enable to shutdown after finishing tests
 */
 @shutdownAferTestsError[atomic]
 +!shutdownAferTests :
     shutdownHook &
     error
     <-
     .print("\n\n");
     .print("**** End of Jason unit tests.\n\n");
     .exit_error;
 .
@shutdownAferTestsSuccess[atomic]
+!shutdownAferTests :
    shutdownHook &
    not intention(_)
    <-
    .print("\n\n");
    .print("**** End of Jason unit tests.\n\n");
    .stopMAS;
.
+!shutdownAferTests. // If auto shutdown is disabled

/**
 * create agents by files present in folder test/agt/
 */
@createTestAgents[atomic]
+!createTestAgents :
    .my_name(testController)
    <-
    .list_files("./inc",".*.asl",IGNORE);
    .list_files("./",".*.asl",FILES);
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
+!createTestAgents. // avoid plan not found for asl that includes controller
