// Agent sender in project ttt

/* Initial beliefs and rules */

/* Initial goals */

!start(0).

/* Plans */

+!start(X) <- .wait(1500); .send(idleag,tell,hi(X)); !!start(X+1).
