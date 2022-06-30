!start.

+!start <-  !g(c,7) |&| (!g(a,9) ||| !g(b,3)).

+!g(_,0).
+!g(L,X) <- .print(L,X); .wait(500); !g(L,X-1).
