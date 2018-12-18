
!registerDF.
+!registerDF <- .df_register("participant").


+N::cnp_started[source(A)]  // receives the announce of a new CNP in namespace N from A
    <-  // adds some beliefs used by the participant module
        +N::price(_,(3*math.random)+10);
        +N::acceptable(fix(N));
        .include("participant.asl",N).
