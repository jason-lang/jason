!start.
+!start : true <- .list_plans; !d(1); !d(2).

{ begin ld }
+!d(X) <- act(X).
+!d(X) <- act(X).
{ end }

/*
The above plans will be changed by the directive to:
  +!d(X) <- .print("Entering ",d(X)); act(X); .print("Leaving ",d(X)).
  +!d(X) <- .print("Entering ",d(X)); act(X); .print("Leaving ",d(X)).
*/

