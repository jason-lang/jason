// Agent maria

vl(1).
vl(2).


/* Plan triggered when an achieve message is received.
   It is like a new goal, but with a different source.
*/
+!goto(X,Y)[source(Ag)] : true
   <- .println("Received achieve ",goto(X,Y)," from ", Ag).

