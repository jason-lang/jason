!start. // initial goal.

+!start : not a(_) 
  <- .print("First run."); 
     +a(1);
     .wait(1000);
     .stopMAS.
     
+!start : a(X) 
  <- -+a(X+1); 
     .print("Not first run, I already run ",X," times.");
     .wait(2000);
     .stopMAS.

