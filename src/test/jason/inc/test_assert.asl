/**
 * Assert helpers
 */

/**
 * Asserts if X is equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals[atomic]
+!assert_equals(X,Y) :
    .current_intention(I) &
    I = intention(ID,_) &
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
    .current_intention(I) &
    I = intention(ID,_)
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
    .current_intention(I) &
    I = intention(ID,_)
    <-
    if (not (Y >= X-T & Y <= X+T)) {
        .log(severe,"Intention ",ID," FAILED! Assert equals expected ",X,"+/-",T,", but had ",Y);
        .fail;
    } else {
        -+test_passed;
        .log(info,"Intention ",ID," PASSED");
    }
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
    .current_intention(I) &
    I = intention(ID,_)
    <-
    if (not X) {
        .log(severe,"Intention ",ID," FAILED! Assert true expected ",X);
        .fail;
    } else {
        -+test_passed;
        .log(info,"Intention ",ID," PASSED");
    }
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
    .current_intention(I) &
    I = intention(ID,_)
    <-
    if (X) {
        .log(severe,"Intention ",ID," FAILED! Assert false expected not ",X);
        .fail;
    } else {
        -+test_passed;
        .log(info,"Intention ",ID," PASSED");
    }
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
    .current_intention(I) &
    I = intention(ID,_)
    <-
    -+test_passed;
    .log(info,"Intention ",ID," PASSED");
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
    .current_intention(I) &
    I = intention(ID,_)
    <-
    .log(severe,"Intention ",ID," forcedly FAILED! Msg: ",MSG);
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
    .current_intention(I) &
    I = intention(ID,_)
    <-
    if (not .member(Y,X)) {
        .log(severe,"Intention ",ID," FAILED! Assert equals expected ",X," but had ",Y);
        .fail;
    }
    -+test_passed;
    .log(info,"Intention ",ID," PASSED");
.
-!assert_contains(X,Y) :
    true
    <-
    -+test_failed;
.
