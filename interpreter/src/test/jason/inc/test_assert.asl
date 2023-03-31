/**
 * Assert helpers
 */

intention_test_goal(Goal,Test,Label,Line,Src) :- .intention(ID,_,[ im(Label,TGoal,{ Test; _ },_)|_],current) &
    (TGoal = {+!Goal[_|_]} | TGoal = {-!Goal[_|_]}) &
    _[code_line(Line),code_src(Src)] = Label.

/**
 * Asserts if X is equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals[atomic]
+!assert_equals(X,Y) : // compare terms
    intention_test_goal(Goal,Test,Label,Line,Src) &
    not .list(X) & not .list(Y)
    <-
    if (X \== Y) {
        .type(X,TX);
        .type(Y,TY);
        .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X,"' (",TX,") but had '",Y,"' (",TY,")");
        .fail;
    } else {
        +test(Test,passed,Src,Line)[assert_equals(X,Y)];
        .log(info,"assert_equals on event '",Goal,"' PASSED");
    }
.
+!assert_equals(X,Y) : // compare lists
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    .type(X,TX);
    .type(Y,TY);
    for ( .member(Xth,X) ) {
        if ( not .member(Xth,Y) ) {
            .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X,"' (",TX,") but had '",Y,"' (",TY,")");
            .fail;
        }
    }
    for ( .member(Yth,Y) ) {
        if ( not .member(Yth,X) ) {
            .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X,"' (",TX,") but had '",Y,"' (",TY,")");
            .fail;
        }
    }
    +test(Test,passed,Src,Line)[assert_equals(X,Y)];
    .log(info,"assert_equals on event '",Goal,"' PASSED");
.
+!assert_equals(X,Y)
    <-
    .type(X,TX);
    .type(Y,TY);
    .log(severe,"assert_equals expecting '",X,"' (",TX,") and having '",Y,"' (",TY,") could not be performed! FAILED!");
    .fail;
.
-!assert_equals(X,Y)
    <-
    +test(Test,failed,Src,Line)[assert_equals(X,Y)];
.

/**
 * Asserts if X is equals to Y with tolerance T
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals_tolerant[atomic]
+!assert_equals(X,Y,T) :
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    .type(X,TX);
    .type(Y,TY);
    if (not (Y >= X-T & Y <= X+T)) {
        .log(severe,"assert_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X,"+/-",T,"' (",TX,"), but had '",Y,"' (",TY,")");
        .fail;
    } else {
        +test(Test,passed,Src,Line)[assert_equals(X,Y,T)];
        .log(info,"assert_equals on event '",Goal,"' PASSED");
    }
.
+!assert_equals(X,Y,T)
    <-
    .type(X,TX);
    .type(Y,TY);
    .log(severe,"assert_equals expecting '",X,"+/-",T,"' (",TX,") and having '",Y,"' (",TY,") could not be performed! FAILED!");
    .fail;
.
-!assert_equals(X,Y,T)
    <-
    +test(Test,failed,Src,Line)[assert_equals(X,Y,T)];
.

/**
 * Asserts if X is not equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_not_equals[atomic]
+!assert_not_equals(X,Y) : // compare terms
    intention_test_goal(Goal,Test,Label,Line,Src) &
    not .list(X) & not .list(Y)
    <-
    .type(X,TX);
    .type(Y,TY);
    if (X == Y) {
        .log(severe,"assert_not_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X,"' (",TX,") different than '",Y,"' (",TY,")");
        .fail;
    } else {
        +test(Test,passed,Src,Line)[assert_equals(X,Y)];
        .log(info,"assert_not_equals on event '",Goal,"' PASSED");
    }
.
+!assert_not_equals(X,Y)
    <-
    .type(X,TX);
    .type(Y,TY);
    .log(severe,"assert_not_equals expecting '",X,"' (",TX,") and having '",Y,"' (",TY,") could not be performed! FAILED!");
    .fail;
.
-!assert_not_equals(X,Y)
    <-
    +test(Test,failed,Src,Line)[assert_not_equals(X,Y)];
.

/**
 * Asserts if X is true / exists
 */
@assert_true[atomic]
+!assert_true(X) :
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    if (not X) {
        .log(severe,"assert_true on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X,"'");
        .fail;
    } else {
        .log(info,"assert_true on event '",Goal,"' PASSED");
        +test(Test,passed,Src,Line)[assert_true(X)];
    }
.
+!assert_true(X)
    <-
    .log(severe,"assert_true expecting '",X,"' could not be performed! FAILED!");
    .fail;
.
-!assert_true(X)
    <-
    +test(Test,failed,Src,Line)[assert_true(X)];
.

/**
 * Asserts if X is false / does not exist
 */
@assert_false[atomic]
+!assert_false(X) :
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    if (X) {
        .log(severe,"assert_false on event '",Goal,"' starting at line ",Line," FAILED! Expected 'not ",X,"'");
        .fail;
    } else {
        +test(Test,passed,Src,Line)[assert_false(X)];
        .log(info,"assert_false on event '",Goal,"' PASSED");
    }
.
+!assert_false(X)
    <-
    .log(severe,"assert_false expecting 'not ",X,"' could not be performed! FAILED!");
    .fail;
.
-!assert_false(X)
    <-
    +test(Test,failed,Src,Line)[assert_false(X)];
.

/**
 * Force a passed test
 */
@force_pass[atomic]
+!force_pass :
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    +test(Test,passed,Src,Line)[force_pass];
    .log(info,"force_pass on event '",Goal,"' PASSED");
.
+!force_pass
    <-
    .log(severe,"force_pass could not be performed properly! FAILED!");
    .fail;
.
-!force_pass // Only pass if not applicable
    <-
    +test(Test,failed,Src,Line)[force_pass];
.

/**
 * Force a failure printing a giving message
 */
@force_failure[atomic]
+!force_failure(MSG) :
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    .log(severe,"force_failure on event '",Goal,"' forcedly FAILED! Msg: '",MSG,"'");
    .fail;
.
+!force_failure(MSG)
    <-
    .log(severe,"force_failure could not be performed properly! FAILED!");
    .fail;
.
-!force_failure(MSG) // Only failure if not applicable
    <-
    +test(Test,failed,Src,Line)[force_failure];
.

/**
 * Asserts if X contains Y
 */
@assert_contains[atomic]
+!assert_contains(X,Y) :
    intention_test_goal(Goal,Test,Label,Line,Src) &
    .findall(T, .type(X,T) ,Types) & not .member(list,Types) & not .member(set,Types)
    <-
    .log(severe,"assert_contains expecting '",Y,"' in '",X,"' could not be performed since '",X,"' must be a list or a set. FAILED!");
    .fail;
.
+!assert_contains(X,Y) :
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    if (not .member(Y,X)) {
        .log(severe,"assert_contains on event '",Goal,"' starting at line ",Line," FAILED! Expected '",Y,"' in '",X,"'");
        .fail;
    }
    +test(Test,passed,Src,Line)[assert_contains(X,Y)];
    .log(info,"assert_contains on event '",Goal,"' PASSED");
.
+!assert_contains(X,Y)
    <-
    .log(severe,"assert_contains expecting '",Y,"' in '",X,"' could not be performed! FAILED!");
    .fail;
.
-!assert_contains(X,Y)
    <-
    +test(Test,failed,Src,Line)[assert_contains(X,Y)];
.

/**
 * Asserts if X does not contain Y
 */
@assert_not_contains[atomic]
+!assert_not_contains(X,Y) :
    intention_test_goal(Goal,Test,Label,Line,Src) &
    .findall(T, .type(X,T) ,Types) & not .member(list,Types) & not .member(set,Types)
    <-
    .log(severe,"assert_not_contains expecting '",Y,"' NOT in '",X,"' could not be performed since '",X,"' must be a list or a set. FAILED!");
    .fail;
.
+!assert_not_contains(X,Y) :
    intention_test_goal(Goal,Test,Label,Line,Src)
    <-
    if ( .member(Y,X) ) {
        .log(severe,"assert_not_contains on event '",Goal,"' starting at line ",Line," FAILED! Expected '",Y,"' NOT in '",X,"'");
        .fail;
    }
    +test(Test,passed,Src,Line)[assert_not_contains(X,Y)];
    .log(info,"assert_not_contains on event '",Goal,"' PASSED");
.
+!assert_not_contains(X,Y)
    <-
    .log(severe,"assert_not_contains expecting '",Y,"' NOT in '",X,"' could not be performed! FAILED!");
    .fail;
.
-!assert_not_contains(X,Y)
    <-
    +test(Test,failed,Src,Line)[assert_not_contains(X,Y)];
.
/**
 * Asserts if X is greater than Y
 */
@assert_greater_than[atomic]
+!assert_greater_than(X,Y) : // compare terms
    intention_test_goal(Goal,Test,Label,Line,Src) &
    .number(X) & .number(Y)
    <-
    if (X <= Y) {
        .log(severe,"assert_greater_than on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X," > ",Y,"'");
        .fail;
    } else {
        +test(Test,passed,Src,Line)[assert_greater_than(X,Y)];
        .log(info,"assert_greater_than on event '",Goal,"' PASSED");
    }
.
+!assert_greater_than(X,Y)
    <-
    .log(severe,"assert_greater_than expecting '",X," > ",Y,"' could not be performed! FAILED!");
    .fail;
.
-!assert_greater_than(X,Y)
    <-
    +test(Test,failed,Src,Line)[assert_greater_than(X,Y)];
.

/**
 * Asserts if X is greater/equal than Y
 */
@assert_greater_than_equals[atomic]
+!assert_greater_than_equals(X,Y) : // compare terms
    intention_test_goal(Goal,Test,Label,Line,Src) &
    .number(X) & .number(Y)
    <-
    if (X < Y) {
        .log(severe,"assert_greater_than_equals on event '",Goal,"' starting at line ",Line," FAILED! Expected '",X," >= ",Y,"'");
        .fail;
    } else {
        +test(Test,passed,Src,Line)[assert_greater_than_equals(X,Y)];
        .log(info,"assert_greater_than_equals on event '",Goal,"' PASSED");
    }
.
+!assert_greater_than_equals(X,Y)
    <-
    .log(severe,"assert_greater_than_equals expecting '",X," >= ",Y,"' could not be performed! FAILED!");
    .fail;
.
-!assert_greater_than_equals(X,Y)
    <-
    +test(Test,failed,Src,Line)[assert_greater_than_equals(X,Y)];
.

@assert_between[atomic]
+!assert_between(X,Y0,Y1) : // compare if X is greater/equal than Y0 and lower/equal than Y1
    intention_test_goal(Goal,Test,Label,Line,Src) &
    .number(X) & .number(Y0) & .number(Y1)
    <-
    if ((X < Y0) | (X > Y1)) {
        .log(severe,"assert_between on event '",Goal,"' starting at line ",Line," FAILED! Expected '",Y0," <= ",X," <= ",Y1,"'");
        .fail;
    } else {
        +test(Test,passed,Src,Line)[assert_between(X,Y0,Y1)];
        .log(info,"assert_between on event '",Goal,"' PASSED");
    }
.
+!assert_between(X,Y0,Y1)
    <-
    .log(severe,"assert_between expecting '",Y0," <= ",X," <= ",Y1,"' could not be performed! FAILED!");
    .fail;
.
-!assert_between(X,Y0,Y1)
    <-
    +test(Test,failed,Src,Line)[assert_between(X,Y0,Y1)];
.
