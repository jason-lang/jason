/**
 * Assert helpers
 */

{ include("test_controller.asl") }

/**
 * Asserts if X is equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assert_equals[atomic]
+!assert_equals(X,Y) :
    .current_intention(I) &
    I = intention(ID,_)
    <-
    if (X \== Y) {
      .print("Intention ",ID," error! Assert equals expected ",X," but had ",Y);
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
    I = intention(ID,_)
    <-
    if (not (Y >= X-T & Y <= X+T)) {
      .print("Intention ",ID," error! Assert equals expected ",X,"+/-",T,", but had ",Y);
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
    I = intention(ID,_)
    <-
    if (not X) {
      .print("Intention ",ID," error! Assert true expected ",X);
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
    I = intention(ID,_)
    <-
    if (X) {
      .print("Intention ",ID," error! Assert false expected not ",X);
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
