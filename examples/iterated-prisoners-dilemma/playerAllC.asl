/*** I'm a player... ***/

{ include("player.asl") }

/*** ... and my strategy is ... ***/

// AllC:
// I always play c
+arrested(T,O)[source(arbitrer)]
  :  true
  <- .send(arbitrer, tell, play(T,c));
     .print("I played c, as always, against ",O," at time ",T).

// I don't need records
+!take_records(T,O,M).

