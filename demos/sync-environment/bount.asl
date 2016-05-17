// this agent has one intention that acts twice

!c(0).

/* Plans */

+!c(500) <- !!c(0).
+!c(X)   <- act; // do some action
            act; // act twice!
            screen.show_counter(X); // show counter
            !!c(X+2). // continue

//+executed_actions(X) : X mod 9 \== 0 <- .println("Problem! Error=", X mod 9).

