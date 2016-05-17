// Agent b in project big.mas2j

/* Initial goals */

!start(0).

/* Plans */

+!start(X) : X < 5 <- a(X); !start(X+1).
+!start(_).

