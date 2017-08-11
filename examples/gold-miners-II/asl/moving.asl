/* plans for moving */

last_dir(null). // the last movement


/* next_step: do an action towards some destination,
   the destination may be unachievable */
+!next_step(X,Y)
   :  pos(AgX,AgY,_)
   <- jia.direction(AgX, AgY, X, Y, D);
      //.print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
      -+last_dir(D);
      do(D).
+!next_step(X,Y) : not pos(_,_,_) // I still do not know my position
   <- !next_step(X,Y).

-!next_step(X,Y) // failure handling -> start again!
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      -+last_dir(null);
      !next_step(X,Y).


/* pos is used when it is algways possible to go */
+!pos(X,Y)
  :  .desire(spos(OX,OY))
  <- .current_intention(I);
     .print("** Trying to go to ",X,",",Y," while another !pos to ",OX,",",OY," is running by intention ",I);
      .fail.
+!pos(X,Y)
  <- jia.set_target(X,Y);
     !spos(X,Y).

+!spos(X,Y) : pos(X,Y,_). // <- .print("I've reached ",X,"x",Y).
+!spos(X,Y) : not jia.obstacle(X,Y) // the obstacle may be discovered after !pos(X,Y), so spos should fail.
  <- !next_step(X,Y);
     !spos(X,Y).

