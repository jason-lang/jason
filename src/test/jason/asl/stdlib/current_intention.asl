/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@test_current_intention[test]
+!test_current_intention
    <-
    .current_intention(intention(Id,[im(L,_,_,_)|_]));
    !assert_true(.substring(test_current_intention,L,0));
.
