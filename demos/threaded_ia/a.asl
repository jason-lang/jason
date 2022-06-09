// goals
!start(0).
!do_task.

+!start(10) <- .stopMAS.
+!start(X) <- .print("I am alive....  ",X); .wait(1000); !start(X+1).

// the following plan runs an internal action that
// suspends the intention, runs a thread, and, when finished, resume the intention

+!do_task <- .print("start...."); mylib.tiac(3000,X); .print("finish with X=",X).
