p(c).
~p(c).

+p(a) : true <- act; .print("Noticed p(a).").

+~p(a) : true <- act; .print("Noticed ~p(a).").

+p(b) : p(a) & ~p(a) <- act; .print("Noticed p(b) and contradiction.").

+p(b) : not p(a) & not ~p(a) <- act; .print("Noticed p(b) and lack of information.").

+p(b) : true <- act; .print("Noticed p(b) and nothing else special.").

+~p(b) : p(a) & ~p(a) <- act; .print("Noticed ~p(b) and contradiction.").

+~p(b) : not p(a) & not ~p(a) <- act; .print("Noticed ~p(b) and lack of information.").

+~p(b) : true <- act; .print("Noticed ~p(b) and nothing else special.").
