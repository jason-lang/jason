/*
 * Test XOR rule
*/

{ include("tester_agent.asl") }
{ include("main/jason/asl/unit.asl") }


@[test]
+!test_xor
    <-
    !assert_false(xor(true, false));
.
