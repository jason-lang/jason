// Agent b in project act-sync.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!c(0).

/* Plans */

+!c(500) <- !!c(0).
+!c(X)   <- act; // do some action
            act; // act twice!
            screen.show_counter(X); // show counter
            !!c(X+1). // continue

+executed_actions(X) : X mod 9 \== 0 <- .println("Problem! ",X).
