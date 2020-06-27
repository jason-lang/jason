!createNew(bob   , 2000,"sn.ser").
!createNew(karlos, 3000,"snk.ser").
!snap(karlos,2000,"snk.ser").

+!createNew(A,T,F)
   <- .wait(T);
      .concat(new,A,NName);
      .term2string(TName,NName);
      .print("resuming ",TName, " from ",F);
      ia.create_ag_sst(TName,F);
.

+!snap(A,T,F)
   <- .wait(T);
      ia.snapshot(A,F)
.
