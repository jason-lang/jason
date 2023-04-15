// this agent always bids 6

@lbid
+auction(N)[source(S)] : true
   <- .send(S, tell, place_bid(N,6)).

