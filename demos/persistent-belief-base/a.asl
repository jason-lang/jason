!start. // initial goal.

+!start : not a(_)
  <- .print("First run.");
     +a(1);
     .stopMAS(3000).

+!start : a(X)
  <- -+a(X+1);
     .print("Not first run, I already run ",X," times.");
     .stopMAS(4000).
