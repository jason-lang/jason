
!start.

+!start : true
   <- .print("creating an artifact...");
      makeArtifact(c1,"tools.Counter",[10],ArtId);
      focus(ArtId);
      .send(alice,achieve,focus(c1));
      .wait(1000);
      inc;
   .
