// Agent a in project big.mas2j

/* Initial goals */

!start(0).

/* Plans */

+!start(X) : X < 100 & (X mod 2 == 0) <- .print("run ",X); !!start(X+1).
+!start(X) : X < 100                  <- !!start(X+1).
+!start(_) : .my_name(a1000) <- .stopMAS.
+!start(_).

