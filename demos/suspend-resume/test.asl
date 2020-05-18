// Agent test in project suspend.mas2j

/* Initial goals */

!dots.
!control.

/* Plans */

// loop printing dots
+!dots
  <- .print(".");
     !!dots.

// another loop that resumes and suspend the "dots" goal
+!control
  <- .wait(20);
     .suspend(dots); // suspend the intention with goal dots
     .println;
     .wait(1000);
     .resume(dots);  // resume it
     !!control.
