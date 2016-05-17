// this agent bids 3,
// if it looses 3 auctions, it proposes an alliance to
// ag2 and therefore it bids 7 (3 from itself + 4 from ag2)

default_bid_value(3).
ally(ag2).
threshold(3).

+auction(N)[source(S)] 
   :  (threshold(T) & N < T) 
      |
      (.my_name(I) & winner(I) & ally(A) & not alliance(I,A))
   <- !bid_normally(S,N).

+auction(N)[source(S)] 
   :  .my_name(I) & not winner(I) & ally(A) & not alliance(I,A)
   <- !alliance(A);
      !bid_normally(S,N).

@palliance
+auction(N)[source(S)] 
   :  alliance(_,A)
   <- ?default_bid_value(B);
      ?bid(A,C);
      .send(S, tell, place_bid(N,B+C)).

+!bid_normally(S,N) : true
   <- ?default_bid_value(B);
      .send(S, tell, place_bid(N,B)).

@prop_alliance[breakpoint]
+!alliance(A) : true
   <- .send(A,tell,alliance).
   
