/* The fetch gold goal */

+!fetch_gold(gold(X,Y))
  <- .print("Handling ",gold(X,Y)," now.");
     ?pos(AgX,AgY,_); jia.path_length(AgX,AgY,X,Y,Dist);
     .my_name(MyN);
     .broadcast(tell, committed_to(gold(X,Y),Dist,MyN));
     !pos(X,Y);
     !ensure(pick,0);
     ?carrying_gold(NG);
     .print("Picked ",gold(X,Y), ", I have ",NG," pieces of gold");
     !remove(gold(X,Y));
     // broadcast that I got the gold(X,Y), to avoid someone
     // else to pursue this gold
     .broadcast(tell,picked(gold(X,Y)));
     !!choose_goal.

// if ensure(pick) failed, decide goal
@fpg[atomic]
-!fetch_gold(G)
  <- .print("failed to catch gold ",G);
     .broadcast(untell, committed_to(G,_,_));
     !remove(G);
     !!choose_goal.


+!ensure(pick, N) : N < 4 & pos(X,Y,_) & not cell(X,Y,gold).
+!ensure(pick, N) : N < 4 & pos(X,Y,_) & cell(X,Y,gold) <- do(pick); !ensure(pick, N+1).

// someone else picked up the gold I am going to go,
// so drops the intention and chose another gold
@ppgd[atomic]
+picked(G)[source(A)]
  :  .desire(fetch_gold(G)) &
     .my_name(Me) & A \== Me
  <- .print(A," has taken ",G," that I am pursuing (or discovered someone else picked)! Failing my intention.");
     .fail_goal(fetch_gold(G)).

// someone else picked up a gold I know about,
// remove from my belief base
+picked(G) <- !remove(G).


// someone committed to some gold I am going to,
// drops the intention and choose another gold
@ctg[atomic]
+committed_to(gold(GX,GY),Dist,A)
  :  .desire(fetch_gold(gold(GX,GY))) &
      pos(X,Y,_) & jia.path_length(X,Y,GX,GY,D) & D > 1 & // I am not so near to drop
      D > Dist  // I am farther than A
  <- .print(A," is committed to ",gold(GX,GY)," that I am pursuing (",X,",",Y,"/",D,")! Failing my intention.");
     .fail_goal(fetch_gold(gold(GX,GY))).

