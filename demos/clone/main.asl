test(1).

!start.

+!start
   <- +a(40);
      .print("expected [bob] 10 :41 ");
      .clone("bob");
      .send(bob,tell,p(10)).

+p(X) : a(V) & test(T)
   <- .print(X," : ",V+T).
