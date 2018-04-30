!start.
!drop.

+!start <- .list_plans; !j; .print(fini).
-!start <- .print(finito).

+!drop  <- .wait(4200); +dc.

/*
    This agent uses a declarative goal identified by
    SMC (Single Minded Commitment).
*/

{ begin smc(j,dc) } // dc is the drop condition
+!j : bel <- .print(a); .wait(1000).
+!j       <- .print(b); .wait(1000).
{ end }


/*

execution:

[c] @l__11[source(self)] +!start <- .list_plans; !j; .print(fini).
[c] @l__12[source(self)] -!start <- .print(finito).
[c] @l__13[source(self)] +!drop <- .wait(4200); +dc.

[c] @l__16[source(self)] +!j : j.
[c] @l__14[source(self)] +!j : bel <- .print(a); .wait(1000); ?j.
[c] @l__15[source(self)] +!j <- .print(b); .wait(1000); ?j.
[c] @l__17[source(self)] +j <- .succeed_goal(j).
[c] @l__18[source(self)] -!j <- !j.
[c] @l__19[source(self)] +!j <- !j.
[c] @l__20[source(self)] +dc <- .fail_goal(j).


[c] b
[c] b
[c] b
[c] b
[c] b
[c] finito

*/
