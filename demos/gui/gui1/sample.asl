/* Initial goals */

!start.
!print(system.time).

/* Plans */


// the internal action gui.yes_no succeed if the user click Yes and fails otherwise
//
// note that this IA blocks only the intention using it (the !start) and not all the
// intentions (the agent continues to print "." while the interface is being shown

+!start <- gui.yes_no("Is it Ok?"); .print(ok); .drop_intention(print(_)).
-!start <- .print(nok).

+!print(S) <- .wait(500); .print(". ", system.time-S); !!print(system.time).


