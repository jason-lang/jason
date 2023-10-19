/**
 * Test failure event, among the tests it intends to cover
 * the ones referred at:
 * http://jason-lang.github.io/doc/faq.html#_why_is_jasons_plan_failure_mechanism_different_from_other_agent_platforms
 */

{ include("tester_agent.asl") }

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
   //This !check_test_fail_event was created just to performed the asserts below. When the problem is solved it can be removed and asserts uncommented
   !check_test_fail_event(ErrorId,Msg,CodeBody,CodeSrc,CodeLine);
   !assert_equals(action_failed,ErrorId);
   !assert_equals("file:./src/test/jason/asl/test_failure_event.asl",CodeSrc);

   .print("Expected error: ", ErrorId, " '",Msg,"' by ",CodeBody," in ",CodeSrc,":",CodeLine);
.

+!check_test_fail_event(E,M,C,S,L)
    <-
    !assert_equals(action_failed,E);
    !assert_equals("file:./src/test/jason/asl/test_failure_event.asl",S);
.

/**
 * Test constraint failed when trying to unifying something false
 */
@[test]
+!test_constraint_failed
    <-
    !create_mock_agent(alessandro);
    .send(alessandro, askOne, value(X10), Y10);
    Y10 = value(Z10)[source(A10)[source(B10)]];
    !assert_equals(alessandro,A10);
    .kill_agent(alessandro);
.
-!test_constraint_failed[error(ErrorId), error_msg(Msg), code(CodeBody), code_src(CodeSrc), code_line(CodeLine)]
   <-
   //This !check_test_fail_event was created just to performed the asserts below. When the problem is solved it can be removed and asserts uncommented
   !assert_equals(constraint_failed,ErrorId);
   !assert_equals("file:./src/test/jason/asl/test_failure_event.asl",CodeSrc);

   .print("Expected error: ", ErrorId, " '",Msg,"' by ",CodeBody," in ",CodeSrc,":",CodeLine);
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
    //This !check_test_generic_fail_event was created just to performed the asserts below. When the problem is solved it can be removed and asserts uncommented
    !check_test_generic_fail_event(ErrorId,Msg,CodeBody,CodeSrc,CodeLine);

    !assert_equals(ia_failed,ErrorId);
    !assert_equals("file:./src/test/jason/asl/test_failure_event.asl",CodeSrc);

    .print("Expected error: ", ErrorId, " '",Msg,"' by ",CodeBody," in ",CodeSrc,":",CodeLine);
.

+!check_test_generic_fail_event(E,M,C,S,L)
    <-
    !assert_equals(ia_failed,E);
    !assert_equals("file:./src/test/jason/asl/test_failure_event.asl",S);
.
