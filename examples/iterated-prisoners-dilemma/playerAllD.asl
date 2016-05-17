/*** I'm a player... ***/

{ include("player.asl") }

/*** ... and my strategy is ... ***/

// AllD:
// I always play d
+arrested(T,O)[source(arbitrer)]
  :  true
  <- .send(arbitrer, tell, play(T,d));
     .print("I played d, as always, against ",O," at time ",T).

// I don't need records
+!take_records(T,O,M).

