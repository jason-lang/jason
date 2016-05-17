/* Initial goals */

!start.

/* Plans */

+!start 
   <- makeArtifact("a0","SomeArt",[10],Id);
      focus(Id);
      .print("Artifact created.").

+percept(X)               // from Jason Environment 
   <- .print("I see ",X);
      a2;                 // action on Jason environment
      .wait(100);
      inc.                // action on CArtAgO environment
      
+count(X)
   <- .print("Count is ",X).      
      
