/*** initial beliefs ***/

my_score(0).

// Knowledge of the PD, shared by all agents
{include("pd_payoffs.asl")}


/*** initial goals ***/

!startup.


/*** plans ***/

// Introduce myself to the arbitrer
+!startup
  :  true
  <- .my_name(Me);
     .send(arbitrer, tell, player(Me)).

// Result of an encounter has just been announced by
// the arbitrer: update my total score, and take records
// if the strategy requires it
@result[atomic]
+score(T,S)[source(arbitrer)]
  :  arrested(T,O)
  <- ?my_score(X);
     -+my_score(X+S);           // update my total score
     -arrested(T,O)[source(_)]; // Don't keep in the BB the whole history of ...
     -score(T,S)[source(_)];    // ... interactions as they are not needed
     ?payoffs(_,M,S,_);         // If I got S points, the move of the opponent was M
     !take_records(T,O,M);      // some strategies might need this information
     my.plot(T,X+S);
     .print("Got ",S," at time ",T,". My total now is: ",X+S).
