
/* Plans */

+hi(X)[source(A)] <- .println; .println("hi ",A," ",X); .println.

+jag_sleeping <- .println("i am going to have a pleasant idleness").
+jag_awaking  <- .println("i am back").

//^!jag_sleeping[state(S)] <- .print("sleep goal state is ",S).
+jag_shutting_down(T) <- .println("I'm gonna die in ",T," ms.").
