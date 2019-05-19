b(10).

!start.

@test +!g1 : b(10) <- ?b(X); .print(X); !g(X).

+!g(X) <- .print(ok,X).

+!start <-
   .relevant_plans({+!g1}, [Plan|_]);
   Plan =.. [L,T,C,B];
   if (C) {
       .print("Start running plan ",L);
       !show_run(B);
   } else {
       .print("Context ",C," not satisfied!");
   }.

+!show_run({})     <- .print("end of execution!").
+!show_run({H; R}) <- .print("command ",H); H; !show_run(R).
