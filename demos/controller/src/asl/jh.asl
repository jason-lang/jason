// Agent jh in project MindInspection

/* Initial beliefs and rules */

bacana(0).

/* Initial goals */

!start(0).

/* Plans */

+!start(X) <- -+bacana(X); !!start(X+1).
