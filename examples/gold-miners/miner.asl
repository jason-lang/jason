// miner agent

/* beliefs */

last_dir(null). // the last movement I did
free.

/* rules */

// next line is the bottom of the quadrant
// if 2 lines bellow is too far
calc_new_y(AgY,QuadY2,QuadY2) :- AgY+2 > QuadY2.

// otherwise, the next line is 2 lines bellow
calc_new_y(AgY,_,Y) :- Y = AgY+2.


/* plans for sending the initial position to leader */

+gsize(S,_,_) : true // S is the simulation Id 
  <- !send_init_pos(S).
+!send_init_pos(S) : pos(X,Y)
  <- .send(leader,tell,init_pos(S,X,Y)).
+!send_init_pos(S) : not pos(_,_) // if I do not know my position yet
  <- .wait("+pos(X,Y)", 500);     // wait for it and try again
     !!send_init_pos(S).

/* plans for wandering in my quadrant when I'm free */

+free : last_checked(X,Y)     <- !prep_around(X,Y).
+free : quadrant(X1,Y1,X2,Y2) <- !prep_around(X1,Y1).
+free : true                  <- !wait_for_quad.

@pwfq[atomic]
+!wait_for_quad : free & quadrant(_,_,_,_) 
   <- -+free.
+!wait_for_quad : free     
   <- .wait("+quadrant(X1,Y1,X2,Y2)", 500); 
      !!wait_for_quad.
+!wait_for_quad : not free 
   <- .print("No longer free while waiting for quadrant.").
-!wait_for_quad // .wait might fail     
   <- !!wait_for_quad.

// if I am around the upper-left corner, move to upper-right corner
+around(X1,Y1) : quadrant(X1,Y1,X2,Y2) & free
  <- .print("in Q1 to ",X2,"x",Y1); 
     !prep_around(X2,Y1).

// if I am around the bottom-right corner, move to upper-left corner
+around(X2,Y2) : quadrant(X1,Y1,X2,Y2) & free 
  <- .print("in Q4 to ",X1,"x",Y1); 
     !prep_around(X1,Y1).

// if I am around the right side, move to left side two lines bellow
+around(X2,Y) : quadrant(X1,Y1,X2,Y2) & free  
  <- ?calc_new_y(Y,Y2,YF);
     .print("in Q2 to ",X1,"x",YF);
     !prep_around(X1,YF).

// if I am around the left side, move to right side two lines bellow
+around(X1,Y) : quadrant(X1,Y1,X2,Y2) & free  
  <- ?calc_new_y(Y,Y2,YF);
     .print("in Q3 to ", X2, "x", YF); 
     !prep_around(X2,YF).

// last "around" was none of the above, go back to my quadrant
+around(X,Y) : quadrant(X1,Y1,X2,Y2) & free & Y <= Y2 & Y >= Y1  
  <- .print("in no Q, going to X1");
     !prep_around(X1,Y).
+around(X,Y) : quadrant(X1,Y1,X2,Y2) & free & X <= X2 & X >= X1  
  <- .print("in no Q, going to Y1");
     !prep_around(X,Y1).

+around(X,Y) : quadrant(X1,Y1,X2,Y2)
  <- .print("It should never happen!!!!!! - go home");
     !prep_around(X1,Y1).

+!prep_around(X,Y) : free
  <- -around(_,_); -last_dir(_); !around(X,Y).
 
+!around(X,Y) 
   :  // I am around to some location if I am near it or
      // the last action was skip (meaning that there are no paths to there)
      (pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y)) | last_dir(skip) 
   <- +around(X,Y).
+!around(X,Y) : not around(X,Y)
   <- !next_step(X,Y);
      !!around(X,Y).
+!around(X,Y) : true 
   <- !!around(X,Y).

+!next_step(X,Y)
   :  pos(AgX,AgY)
   <- jia.get_direction(AgX, AgY, X, Y, D);
      //.print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
      -+last_dir(D);
      do(D).
+!next_step(X,Y) : not pos(_,_) // I still do not know my position
   <- !next_step(X,Y).
-!next_step(X,Y) : true  // failure handling -> start again!
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      -+last_dir(null);
      !next_step(X,Y).


/* Gold-searching Plans */

// I perceived unknown gold and I am free, handle it
@pcell[atomic]           // atomic: so as not to handle another 
                         // event until handle gold is initialised
+cell(X,Y,gold) 
  :  not carrying_gold & free
  <- -free;
     +gold(X,Y);
     .print("Gold perceived: ",gold(X,Y));
     !init_handle(gold(X,Y)).
     
// if I see gold and I'm not free but also not carrying gold yet
// (I'm probably going towards one), abort handle(gold) and pick up
// this one which is nearer
@pcell2[atomic]
+cell(X,Y,gold)
  :  not carrying_gold & not free &
     .desire(handle(gold(OldX,OldY))) &   // I desire to handle another gold which
     pos(AgX,AgY) &
     jia.dist(X,   Y,   AgX,AgY,DNewG) &
     jia.dist(OldX,OldY,AgX,AgY,DOldG) &
     DNewG < DOldG                        // is farther than the one just perceived
  <- +gold(X,Y);
     .drop_desire(handle(gold(OldX,OldY)));
     .print("Giving up current gold ",gold(OldX,OldY),
            " to handle ",gold(X,Y)," which I am seeing!");
     .print("Announcing ",gold(OldX,OldY)," to others");
     .broadcast(tell,gold(OldX,OldY));
     .broadcast(untell, committed_to(gold(OldX,OldY)));
     !init_handle(gold(X,Y)).
     
// I am not free, just add gold belief and announce to others
+cell(X,Y,gold) 
  :  not gold(X,Y) & not committed_to(gold(X,Y))
  <- +gold(X,Y);
     .print("Announcing ",gold(X,Y)," to others");
     .broadcast(tell,gold(X,Y)). 
     
// someone else sent me a gold location
+gold(X1,Y1)[source(A)]
  :  A \== self &
     not allocated(gold(X1,Y1),_) & // The gold was not allocated yet
     not carrying_gold &            // I am not carrying gold
     free &                         // and I am free
     pos(X2,Y2) &
     .my_name(Me)
  <- jia.dist(X1,Y1,X2,Y2,D);       // bid
     .send(leader,tell,bid(gold(X1,Y1),D,Me)).

// bid high as I'm not free
+gold(X1,Y1)[source(A)]
  :  A \== self & .my_name(Me)
  <- .send(leader,tell,bid(gold(X1,Y1),10000,Me)).

// gold allocated to me
@palloc1[atomic]
+allocated(Gold,Ag)[source(leader)] 
  :  .my_name(Ag) & free // I am still free
  <- -free;
     .print("Gold ",Gold," allocated to ",Ag);
     !init_handle(Gold).

// some gold was allocated to me, but I can not
// handle it anymore, re-announce
@palloc2[atomic]
+allocated(Gold,Ag)[source(leader)] 
  :  .my_name(Ag) & not free // I am no longer free
  <- .print("I can not handle ",Gold," anymore!");
     .print("(Re)announcing ",Gold," to others");
     .broadcast(tell,Gold). 
     
     
// someone else picked up the gold I am going to go,
// so drops the intention and chose another gold
@ppgd[atomic]
+picked(G)[source(A)] 
  :  .desire(handle(G)) | .desire(init_handle(G))
  <- .print(A," has taken ",G," that I am pursuing! Dropping my intention.");
     .abolish(G);
     .drop_desire(handle(G)); 
     !!choose_gold.

// someone else picked up a gold I know about, 
// remove from my belief base
+picked(gold(X,Y))
  <- -gold(X,Y)[source(_)].


@pih1[atomic]
+!init_handle(Gold) 
  :  .desire(around(_,_)) 
  <- .print("Dropping around(_,_) desires and intentions to handle ",Gold);
     .drop_desire(around(_,_));
     !init_handle(Gold).
@pih2[atomic]
+!init_handle(Gold)
  :  pos(X,Y)
  <- .print("Going for ",Gold);
     -+last_checked(X,Y);
     !!handle(Gold). // must use !! to perform "handle" as not atomic

+!handle(gold(X,Y)) 
  :  not free 
  <- .print("Handling ",gold(X,Y)," now.");
     .broadcast(tell, committed_to(gold(X,Y)));
     !pos(X,Y);
     !ensure(pick,gold(X,Y));
     // broadcast that I got the gold(X,Y), to avoid someone 
     // else to pursue this gold
     .broadcast(tell,picked(gold(X,Y)));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop, 0);
     -gold(X,Y)[source(_)]; 
     .print("Finish handling ",gold(X,Y));
     !!choose_gold.

// if ensure(pick/drop) failed, pursue another gold
-!handle(G) : G
  <- .print("failed to catch gold ",G);
     .abolish(G); // ignore source
     !!choose_gold.
-!handle(G) : true
  <- .print("failed to handle ",G,", it isn't in the BB anyway");
     !!choose_gold.

// no known gold to choose from
// become free again to search for gold
+!choose_gold 
  :  not gold(_,_)
  <- -+free.

// Finished one gold, but others left
// find the closest gold among the known options, 
// that nobody else committed to
+!choose_gold 
  :  gold(_,_)
  <- .findall(gold(X,Y),gold(X,Y),LG);
     !calc_gold_distance(LG,LD);
     .length(LD,LLD); LLD > 0;
     .print("Uncommitted gold distances: ",LD,LLD);
     .min(LD,d(_,NewG));
     .print("Next gold is ",NewG);
     !!handle(NewG).
-!choose_gold <- -+free.

+!calc_gold_distance([],[]).
+!calc_gold_distance([gold(GX,GY)|R],[d(D,gold(GX,GY))|RD]) 
  :  pos(IX,IY) & not committed_to(gold(GX,GY))
  <- jia.dist(IX,IY,GX,GY,D);
     !calc_gold_distance(R,RD).
+!calc_gold_distance([_|R],RD) 
  <- !calc_gold_distance(R,RD).


// BCG!
// !pos is used when it is algways possible to go 
// so this plans should not be used: +!pos(X,Y) : last_dir(skip) <-
// .print("It is not possible to go to ",X,"x",Y).
// in the future
//+last_dir(skip) <- .drop_goal(pos) 
+!pos(X,Y) : pos(X,Y) <- .print("I've reached ",X,"x",Y).
+!pos(X,Y) : not pos(X,Y)
  <- !next_step(X,Y);
     !pos(X,Y).


+!ensure(pick,_) : pos(X,Y) & cell(X,Y,gold)
  <- do(pick); ?carrying_gold. 
// fail if no gold there or not carrying_gold after pick! 
// handle(G) will "catch" this failure.

+!ensure(drop, _) : pos(X,Y) & depot(_,X,Y) 
  <- do(drop). //TODO: not ?carrying_gold. 


/* end of a simulation */

+end_of_simulation(S,_) : true 
  <- .drop_all_desires; 
     .abolish(quadrant(_,_,_,_));
     .abolish(gold(_,_));
     .abolish(committed_to(_));
     .abolish(picked(_));
     .abolish(last_checked(_,_));
     -+free;
     .print("-- END ",S," --").

