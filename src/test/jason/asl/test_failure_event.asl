/**
 * Test failure event, among the tests it intends to cover
 * the ones referred at:
 * http://jason.sourceforge.net/doc/faq.html#_why_is_jasons_plan_failure_mechanism_different_from_other_agent_platforms
 */

{ include("$jasonJar/test/jason/inc/tester_agent.asl") }

!execute_test_plans.

/**
 * Test fail event and action_failed
 */
@[test]
+!test_fail_event
    <-
    non_existing_action;
.
-!test_fail_event[error(ErrorId), error_msg(Msg), code(CodeBody), code_src(CodeSrc), code_line(CodeLine)]
   <-
   .log(warning,"TODO: Remove !check_test_fail_event and uncomment asserts, notice the output says assert_equals on event 'unknown', i.e., -!events has no intention? Due to this issue all asserts return failure!");

   //This !check_test_fail_event was created just to performed the asserts below. When the problem is solved it can be removed and asserts uncommented
   !check_test_fail_event(ErrorId,Msg,CodeBody,CodeSrc,CodeLine);
   //!assert_equals(action_failed,ErrorId);
   //!assert_equals("test_failure_event.asl",CodeSrc);

   .print("Expected error: ", ErrorId, " '",Msg,"' by ",CodeBody," in ",CodeSrc,":",CodeLine);
.

+!check_test_fail_event(E,M,C,S,L)
    <-
    !assert_equals(action_failed,E);
    !assert_equals("test_failure_event.asl",S);
.

/**
 * Test generic fail event and ia_failed
 */
@[test]
+!test_generic_fail_event
    <-
    .fail;
.
-!F[error(ErrorId), error_msg(Msg), code(CodeBody), code_src(CodeSrc), code_line(CodeLine)]
    <-
    .log(warning,"TODO: Remove !check_test_fail_event and uncomment asserts, notice the output says assert_equals on event 'unknown', i.e., -!events has no intention? Due to this issue all asserts return failure!");

    //This !check_test_generic_fail_event was created just to performed the asserts below. When the problem is solved it can be removed and asserts uncommented
    !check_test_generic_fail_event(ErrorId,Msg,CodeBody,CodeSrc,CodeLine);

    //!assert_equals(ia_failed,ErrorId);
    //!assert_equals("test_failure_event.asl",CodeSrc);

    .print("Expected error: ", ErrorId, " '",Msg,"' by ",CodeBody," in ",CodeSrc,":",CodeLine);
.

+!check_test_generic_fail_event(E,M,C,S,L)
    <-
    !assert_equals(ia_failed,E);
    !assert_equals("test_failure_event.asl",S);
.
