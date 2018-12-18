{begin namespace(supp,local)}
    price(bridge,300).
    price(park,  150).
    gain(hall,   1.5).
    gain(comm,   0.8).
{end}

hall::acceptable(build(_)).
comm::acceptable(build(_)).

!registerDF.
+!registerDF <- .df_register("participant").

+N::cnp_started[source(bob)]
     : .member(N,[hall,comm])
    <- + {+?N::price(build(T),P) : supp::gain(N,G) <- ?supp::price(T,M); P = M*(1+G)};
       .include("participant.asl",N).
