/* Initial goals */

!start.

/* Plans */

+!start <- gui.create("Example GUI").

// the event !run is created by the GUI
+!run(X) <- .print("running ",X,"...."); .wait(500); !!run(X).


// the event !stop is created by the GUI
+!stop(X) <- .drop_desire(run(X)); .print("stop ",X).
