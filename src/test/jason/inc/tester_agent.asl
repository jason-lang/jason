/**
 * Tester agent is an agent managed by test_manager
 */

{ include("test_manager.asl") }
{ include("test_assert.asl") }

/**
 * Send data to test_manager
 */
@test_passed[atomic]
+test_passed :
    true
    <-
    .send(test_manager,achieve,count_tests(passed));
.

@test_failed[atomic]
+test_failed :
    true
    <-
    .send(test_manager,achieve,count_tests(failed));
.
