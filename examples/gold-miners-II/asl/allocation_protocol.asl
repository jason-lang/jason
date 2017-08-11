/* gold allocation protocol */

// someone else sent me a gold location, send a bid
+gold(X,Y)[source(A)]
  :  A \== self
  <- !calc_bid(gold(X,Y), Bid);
     .my_name(Me);
     //.print("My bid for ",gold(X,Y)," is ",Bid);
     .send(leader,tell,bid(gold(X,Y),Bid,Me)).

/*
// bid in case I am going to depot:
// the distance (and fatigue) of going to the gold and after to depot
// -- this strategy leaves good gold options unhandled
+!calc_bid(gold(X,Y), Bid)
  :  container_has_space &
     .desire(goto_depot) & depot(_,DX,DY) &
     pos(AgX,AgY,_) & carrying_gold(N)
  <- jia.path_length(AgX,AgY,X,Y,D1); jia.add_fatigue(D1,N,U1);
     jia.path_length(X,Y,DX,DY,D2);   jia.add_fatigue(D1,N+1,U2);
     Bid = U1 + U2.
*/

// bid in case I have space and known golds: the distance if the new gold are good
+!calc_bid(gold(GX,GY), Bid)
  :  container_has_space &
     .findall(gold(X,Y),gold(X,Y),LG) &
     LG \== [] &                            // i know some gold location
     pos(AgX,AgY,_) &
     calc_gold_distance(LG,LD) &            // get the distance to all known golds
     .min(LD,d(BestGoldDist,BestGold)) &    // the distance of best gold
     jia.path_length(AgX,AgY,GX,GY,GDist)   // my distance to gold to bid
     //.print("G=",gold(GX,GY),"/",GDist," Best=",BestGold,"/",BestGoldDist);
  <- GDist <= BestGoldDist; // should fail in this case
     //!worthwhile(gold(GX,GY));
     jia.add_fatigue(GDist,Bid).

// bid in case I have space and not known golds: the distance to gold
+!calc_bid(gold(GX,GY), Bid)
  :  container_has_space
  <- ?pos(AgX,AgY,_);
     jia.path_length(AgX,AgY,GX,GY,GDist);  // my distance to gold
     jia.add_fatigue(GDist,Bid).

// bid high otherwise (I do not have space)
+!calc_bid(_, 10000).
-!calc_bid(_, 11000).

// calculates the agent distance for a list of golds.
calc_gold_distance([],[]) :- true.
calc_gold_distance([gold(GX,GY)|R],[d(D,gold(GX,GY))|RD])
  :- pos(AgX,AgY,_) &
     jia.path_length(AgX,AgY,GX,GY,D) &
     calc_gold_distance(R,RD).


// some gold was allocated to me, but I can not
// handle it anymore, re-announce
@palloc2[atomic]
+allocated(Gold,Me)[source(leader)]
  :  .my_name(Me) &                    // the gold was allocated to me
     not container_has_space           // I no longer have space
  <- .print("I can not handle ",Gold," anymore! (Re)announcing to others");
     .abolish(allocated(Gold,Me));
     .broadcast(tell,Gold).

// some gold was allocated to me, but I have too much
// allocations. Choose the worst to discard.
@palloc3[atomic]
+allocated(Gold,Me) // even by myself
  :  .my_name(Me) &                                    // the gold was allocated to me
     .findall(G,allocated(G,Me),LAlloc) &
     .length(LAlloc,S) & my_capacity(Cap) & S > Cap+1  // more allocations than I support
  <- ?calc_gold_distance(LAlloc,LD);
     .sort(LD,LDS);
     .length(LD,LDL);
     .nth(LDL-1,LDS,d(_,GDiscarted1));
     .nth(LDL-2,LDS,d(_,GDiscarted2));
     .print("I have too many allocations (",LDS,"), discarting ",GDiscarted1," and ",GDiscarted2,".");
     .abolish(allocated(GDiscarted1,Me));
     .abolish(allocated(GDiscarted2,Me));
     .broadcast(tell,GDiscarted1);
     .broadcast(tell,GDiscarted2);
     !choose_goal.

// some gold was allocated to me by the leader.
@palloc4[atomic]
+allocated(Gold,Me)[source(leader)]
  :  .my_name(Me)
  <- .print("Gold ",Gold," allocated to me. (Re)deciding what to do.");
     !choose_goal.

