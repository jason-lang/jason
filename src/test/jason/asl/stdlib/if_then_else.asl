/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_if :
    true
    <-
    +vl(11);
    if (vl(X) & X > 10) { // where vl(X) is a belief
        !force_pass;
    } else {
        !force_failure;
    }
.

/**
 * Test if statement with equals and unification
 */
 @[atomic,test]
 +!test_if_equals_and_unify :
    true
    <-
    R=success;

    // Try equals
    if (R == success) {
        !force_pass;
    } else {
        !force_failure;
    }
    if (R == something_else) {
        !force_failure;
    } else {
        !force_pass;
    }

    // Try unification
    if (R = success) {
        !force_pass;
    } else {
        !force_failure;
    }
    if (R = something_else) {
        !force_failure;
    } else {
        !force_pass;
    }
.
