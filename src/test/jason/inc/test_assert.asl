/**
 * Assert helpers
 */

/**
 * Asserts if X is equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals[atomic]
+!assert_equals(X,Y) :
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label &
    not .list(X) & not .list(Y)
    <-
    if (X \== Y) {
        .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected ",X," but had ",Y);
        .fail;
    } else {
        -+test_passed;
        .log(info,"assert_equals on goal '",Goal,"' PASSED");
    }
.
+!assert_equals(X,Y) :
    .intention(ID,_,[ im(_,{+!Goal[An]},_,_)|_],current)
    <-
    for (.member(Xth,X)) {
        if (not .member(Xth,Y)) {
            .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected ",X," but had ",Y);
            .fail;
        }
    }
    for (.member(Yth,Y)) {
        if (not .member(Yth,X)) {
            .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected ",X," but had ",Y);
            .fail;
        }
    }
    -+test_passed;
    .log(info,"assert_equals on goal '",Goal,"' PASSED");
.
+!assert_equals(X,Y) :
    true
    <-
    .log(severe,"assert_equals on event 'unknown' starting at line ",Line," FAILED! Expected ",X," but had ",Y);
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
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label
    <-
    if (not (Y >= X-T & Y <= X+T)) {
        .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected ",X,"+/-",T,", but had ",Y);
        .fail;
    } else {
        -+test_passed;
        .log(info,"assert_equals on goal '",Goal,"' PASSED");
    }
.
+!assert_equals(X,Y,T) :
    true
    <-
    .log(severe,"assert_equals on event 'unknown' starting at line ",Line," FAILED! Expected ",X,"+/-",T,", but had ",Y);
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
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label
    <-
    if (not X) {
        .log(severe,"assert_true on event '",Goal,"' starting at line ",Line," FAILED! Expected ",X);
        .fail;
    } else {
        -+test_passed;
        .log(info,"assert_true on goal '",Goal,"' PASSED");
    }
.
+!assert_true(X) :
    true
    <-
    .log(severe,"assert_true on event 'unknown' starting at line ",Line," FAILED! Expected ",X);
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
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label
    <-
    if (X) {
        .log(severe,"assert_false on event '",Goal,"' starting at line ",Line," FAILED! Expected not ",X);
        .fail;
    } else {
        -+test_passed;
        .log(info,"assert_false on goal '",Goal,"' PASSED");
    }
.
+!assert_false(X) :
    true
    <-
    .log(severe,"assert_false on event 'unknown' starting at line ",Line," FAILED! Expected not ",X);
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
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label
    <-
    -+test_passed;
    .log(info,"force_pass on goal '",Goal,"' PASSED");
.
+!force_pass :
    true
    <-
    .log(severe,"force_pass on event 'unknown' starting at line ",Line," FAILED!");
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
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label
    <-
    .log(severe,"force_failure on goal '",Goal,"' forcedly FAILED! Msg: ",MSG);
    .fail;
.
+!force_failure(MSG) :
    true
    <-
    .log(severe,"force_failure on event 'unknown' starting at line ",Line," FAILED!");
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
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label
    <-
    if (not .member(Y,X)) {
        .log(severe,"assert_contains on event '",Goal,"' starting at line ",Line," FAILED! Expected ",Y," in ",X);
        .fail;
    }
    -+test_passed;
    .log(info,"assert_contains on goal '",Goal,"' PASSED");
.
+!assert_contains(X,Y) :
    true
    <-
    .log(severe,"assert_contains on event 'unknown' starting at line ",Line," FAILED! Expected ",Y," in ",X);
    .fail;
.
-!assert_contains(X,Y) :
    true
    <-
    -+test_failed;
.
