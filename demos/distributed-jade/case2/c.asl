/* Initial goals */

!start.

/* Plans */

+!start
    <- .print("hello world.");
       !wait_for(a).
+!wait_for(A) : .all_names(L) & .member(A,L)
    <- .send(A,tell,hello).
+!wait_for(A)
    <- .print(A," is not in the system, waiting for it ....");
       .wait(1000);
       !wait_for(A).

