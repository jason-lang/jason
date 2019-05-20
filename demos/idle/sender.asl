!start(0).
!end.
+!start(X) <- .wait(2500); .send(idleag,signal,hi(X)); !start(X+1).

+!end <- .wait(10000); .stopMAS(2000).

+jag_shutting_down(T) <- .print("I have ",T/1000," seconds to pack my stuff.").
