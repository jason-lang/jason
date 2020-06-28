/**
 * Assert helpers
 */

{ include("testController.asl") }

getIntentionId(I,ID) :- I =.. A & .nth(2,A,B) & .nth(0,B,ID).

/**
 * Assert if X is equals to Y
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assertEquals[atomic]
+!assertEquals(X,Y) :
    .current_intention(I) &
    getIntentionId(I,ID)
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
-!assertEquals(X,Y) :
    true
    <-
    .send(testController,tell,error);
.

/**
 * Assert if X is equals to Y with tolerance T
 * IMPORTANT! Do no use this method to compare float numbers
 */
@assertEqualsWithTolerance[atomic]
+!assertEquals(X,Y,T) :
    .current_intention(I) &
    getIntentionId(I,ID)
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
-!assertEquals(X,Y,T) :
    true
    <-
    .send(testController,tell,error);
.

/**
 * Assert if X is true / exists
 */
@assertTrue[atomic]
+!assertTrue(X) :
    .current_intention(I) &
    getIntentionId(I,ID)
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
-!assertTrue(X) :
    true
    <-
    .send(testController,tell,error);
.

/**
 * Assert if X is false / does not exist
 */
@assertFalse[atomic]
+!assertFalse(X) :
    .current_intention(I) &
    getIntentionId(I,ID)
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
-!assertFalse(X) :
    true
    <-
    .send(testController,tell,error);
.
