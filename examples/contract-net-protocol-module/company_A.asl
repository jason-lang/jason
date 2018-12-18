+N::cnp_started[source(A)]                // receives the announce of a new CNP in namespace N from A
    <-  .include("participant.asl",N);
        +N::price(_,(3*math.random)+10);  // adds some beliefs used by the participant module
        +N::acceptable(fix(_));
        !N::joinCNP[source(A)].           // the participant module has plans for the goal joinCNP
