/**
 * Assert helpers
 */

/**
 * Asserts if X is equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals[atomic]
+!assert_equals(X,Y) :
    .intention(ID,_,_,current) &
    not .list(X) & not .list(Y)
    <-
    if (X \== Y) {
        .log(severe,"Intention ",ID," FAILED! Assert equals expected ",X," but had ",Y);
        .fail;
    } else {
        -+test_passed;
        .log(info,"Intention ",ID," PASSED");
    }
.
+!assert_equals(X,Y) :
    .intention(ID,_,_,current)
    <-
    for (.member(Xth,X)) {
        if (not .member(Xth,Y)) {
            .log(severe,"Intention ",ID," FAILED! Assert equals expected ",X," but had ",Y);
            .fail;
        }
    }
    for (.member(Yth,Y)) {
        if (not .member(Yth,X)) {
            .log(severe,"Intention ",ID," FAILED! Assert equals expected ",X," but had ",Y);
            .fail;
        }
    }
    -+test_passed;
    .log(info,"Intention ",ID," PASSED");
.
+!assert_equals(X,Y) :
    true
    <-
    .log(severe,"Intention 'unknown' FAILED! Assert equals expected ",X," but had ",Y);
    .fail;
.
-!assert_equals(X,Y) :
    true
    <-
    -+test_failed;
.

/**
 * Asserts if X is equals to Y with tolerance T
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals_tolerant[atomic]
+!assert_equals(X,Y,T) :
    .intention(ID,_,_,current)
    <-
    if (not (Y >= X-T & Y <= X+T)) {
        .log(severe,"Intention ",ID," FAILED! Assert equals expected ",X,"+/-",T,", but had ",Y);
        .fail;
    } else {
        -+test_passed;
        .log(info,"Intention ",ID," PASSED");
    }
.
+!assert_equals(X,Y,T) :
    true
    <-
    .log(severe,"Intention 'unknown' FAILED! Assert equals expected ",X,"+/-",T,", but had ",Y);
    .fail;
.
-!assert_equals(X,Y,T) :
    true
    <-
    -+test_failed;
.

/**
 * Asserts if X is true / exists
 */
@assert_true[atomic]
+!assert_true(X) :
    .intention(ID,_,_,current)
    <-
    if (not X) {
        .log(severe,"Intention ",ID," FAILED! Assert true expected ",X);
        .fail;
    } else {
        -+test_passed;
        .log(info,"Intention ",ID," PASSED");
    }
.
+!assert_true(X) :
    true
    <-
    .log(severe,"Intention 'unknown' FAILED! Assert true expected ",X);
    .fail;
.
-!assert_true(X) :
    true
    <-
    -+test_failed;
.

/**
 * Asserts if X is false / does not exist
 */
@assert_false[atomic]
+!assert_false(X) :
    .intention(ID,_,_,current)
    <-
    if (X) {
        .log(severe,"Intention ",ID," FAILED! Assert false expected not ",X);
        .fail;
    } else {
        -+test_passed;
        .log(info,"Intention ",ID," PASSED");
    }
.
+!assert_false(X) :
    true
    <-
    .log(severe,"Intention 'unknown' FAILED! Assert false expected not ",X);
    .fail;
.
-!assert_false(X) :
    true
    <-
    -+test_failed;
.

/**
 * Force a passed test
 */
@force_pass[atomic]
+!force_pass :
    .intention(ID,_,_,current)
    <-
    -+test_passed;
    .log(info,"Intention ",ID," PASSED");
.
+!force_pass :
    true
    <-
    .log(severe,"Intention 'unknown' FAILED! force_pass");
    .fail;
.
-!force_pass : // Only pass if not applicable
    true
    <-
    -+test_failed;
.

/**
 * Force a failure printing a giving message
 */
@force_failure[atomic]
+!force_failure(MSG) :
    .intention(ID,_,_,current)
    <-
    .log(severe,"Intention ",ID," forcedly FAILED! Msg: ",MSG);
    .fail;
.
+!force_failure(MSG) :
    true
    <-
    .log(severe,"Intention 'unknown' FAILED! force_failure");
    .fail;
.
-!force_failure(MSG) : // Only failure if not applicable
    true
    <-
    -+test_failed;
.

/**
 * Asserts if X contains Y
 */
@assert_contains[atomic]
+!assert_contains(X,Y) :
    .intention(ID,_,_,current)
    <-
    if (not .member(Y,X)) {
        .log(severe,"Intention ",ID," FAILED! Assert contains expected ",Y," in ",Y);
        .fail;
    }
    -+test_passed;
    .log(info,"Intention ",ID," PASSED");
.
+!assert_contains(X,Y) :
    true
    <-
    .log(severe,"Intention 'unknown' FAILED! Assert contains expected ",Y," in ",Y);
    .fail;
.
-!assert_contains(X,Y) :
    true
    <-
    -+test_failed;
.
