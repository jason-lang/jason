!createNew(bob,"sn.ser").

+!createNew(A,F)
   <- .wait(2000);
      .print("resuming ",A, " from ",F);
      ia.create_ag_sst(newbob,"sn.ser");
.
