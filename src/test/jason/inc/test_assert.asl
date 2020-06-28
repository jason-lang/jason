/**
 * Assert helpers
 */

{ include("test_controller.asl") }

intention_id(I,ID) :- I =.. A & .nth(2,A,B) & .nth(0,B,ID).

/**
 * Asserts if X is equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals[atomic]
+!assert_equals(X,Y) :
    .current_intention(I) &
    intention_id(I,ID)
    <-
    if (X \== Y) {
      .print("Error on assenting equals! Expected: ",X,". Actual:",Y);
      .fail;
    } else {
      if (verbose) {
        .print("Intention ",ID," passed!");
      }
    }
.
-!assert_equals(X,Y) :
    true
    <-
    .send(test_controller,tell,error);
.

/**
 * Asserts if X is equals to Y with tolerance T
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals_tolerant[atomic]
+!assert_equals(X,Y,T) :
    .current_intention(I) &
    intention_id(I,ID)
    <-
    if (not (Y >= X-T & Y <= X+T)) {
      .print("Error on assenting equals! Expected: ",X,"+/-",T,". Actual:",Y);
      .fail;
    } else {
      if (verbose) {
        .print("Intention ",ID," passed!");
      }
    }
.
-!assert_equals(X,Y,T) :
    true
    <-
    .send(test_controller,tell,error);
.

/**
 * Asserts if X is true / exists
 */
@assert_true[atomic]
+!assert_true(X) :
    .current_intention(I) &
    intention_id(I,ID)
    <-
    if (not X) {
      .print("Error on assenting true! Expected: ",X);
      .fail;
    } else {
      if (verbose) {
        .print("Intention ",ID," passed!");
      }
    }
.
-!assert_true(X) :
    true
    <-
    .send(test_controller,tell,error);
.

/**
 * Asserts if X is false / does not exist
 */
@assert_false[atomic]
+!assert_false(X) :
    .current_intention(I) &
    intention_id(I,ID)
    <-
    if (X) {
      .print("Error on assenting false! Expected not: ",X);
      .fail;
    } else {
      if (verbose) {
        .print("Intention ",ID," passed!");
      }
    }
.
-!assert_false(X) :
    true
    <-
    .send(test_controller,tell,error);
.
