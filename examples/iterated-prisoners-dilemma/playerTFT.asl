/*** I'm a player... ***/

{include("player.asl")}

/*** ... who needs to remember the oponent's last move ... ***/

@records[atomic]
+!take_records(T,O,M)
  :  true
  <- -last_move(O,_); // NB: cannot use -+ here!
     +last_move(O,M).
 
/*** ... as my strategy is ... ***/

// TFT:
// (1) play oponent's last move
+arrested(T,O)[source(arbitrer)]
  :  last_move(O,LM)
  <- .send(arbitrer, tell, play(T,LM));
     .print("I played ",LM," against ",O," at time ",T).

// (2) be nice the first time you meet someone
+arrested(T,O)[source(arbitrer)]
  :  true
  <- .send(arbitrer, tell, play(T,c));
     .print("I played c against ",O," at time ",T).

