// This agent usually bids 4,
// when it has an alliance with ag3, it bids 0

default_bid_value(4).
ally(ag3).

+auction(N)[source(S)] : not alliance
   <- ?default_bid_value(B);
      .send(S, tell, place_bid(N,B)).

+auction(N)[source(S)] : alliance
   <- .send(S, tell, place_bid(N,0)).

// alliance proposal from another agent
+alliance[source(A)]
   :  .my_name(I) & ally(A)
   <- .print("Alliance proposed by ", A);
      ?default_bid_value(B);
      .send(A,tell,bid(I,B));
      .send(A,tell,alliance(A,I)).

