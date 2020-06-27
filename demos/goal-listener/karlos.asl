!start(5).
!g.
!g.

+!start(0).
+!start(X) <- .print(X); !g; !start(X-1).

+!g <- .print(g).
