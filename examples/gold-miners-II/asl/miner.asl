// miner agent

{ include("moving.asl") }               // plans for movements in the scenario
{ include("search_unvisited.asl") }     // plans for finding gold
{ include("search_quadrant.asl") }      // idem
{ include("fetch_gold.asl") }           // plans for fetch gold goal
{ include("goto_depot.asl") }           // plans for go to depot goal
{ include("allocation_protocol.asl") }  // plans for the gold allocation protocol

/* functions */

{ register_function("carrying.gold",0,"carrying_gold") }
{ register_function("jia.path_length",4,"jia.path_length") }

/* beliefs */

free.
my_capacity(3).


search_gold_strategy(near_unvisited). // initial strategy
//search_gold_strategy(quadrant).


/* Initial goal (triggered when step 0 is perceived) */
+pos(_,_,0) 
  <- ?gsize(S,_,_);
     .print("Starting simulation ", S);
     !inform_gsize_to_leader(S);
     !choose_goal.
     
+!inform_gsize_to_leader(S) : .my_name(miner1) 
   <- ?depot(S,DX,DY);
      .send(leader,tell,depot(S,DX,DY));
      ?gsize(S,W,H); 
      .send(leader,tell,gsize(S,W,H)).
+!inform_gsize_to_leader(_).


/* 
   decide the goal transition 
     . pursue known gold, or 
     . to go to depot to drop golds, 
     . search gold 
*/

// Others golds left and I have space
// find the closest gold among the known options
@cgod2[atomic]
+!choose_goal
 :  container_has_space &               // I have space for more gold
    .findall(gold(X,Y),gold(X,Y),LG) &  // LG is all known golds
    evaluate_golds(LG,LD) &             // evaluate golds in LD
    .print("All golds=",LG,", evaluation=",LD) &
    .length(LD) > 0 &                   // is there a gold to fetch?
    .min(LD,d(D,NewG,_)) &              // get the near
    worthwhile(NewG)
 <- .print("Gold options are ",LD,". Next gold is ",NewG);
    !change_to_fetch(NewG).

+!choose_goal // there is no worth gold
 :  carrying_gold(NG) & NG > 0
 <- !change_to_goto_depot.

+!choose_goal // not carrying gold, be free and search gold
 <- !change_to_search.
     

// change to goto_depot goal

+!change_to_goto_depot             // nothing to do.
  :  .desire(goto_depot)
  <- .print("do not need to change to goto_depot").
+!change_to_goto_depot             // drop fetch first
  :  .desire(fetch_gold(G))
  <- .drop_desire(fetch_gold(G)); 
     !change_to_goto_depot.
+!change_to_goto_depot             // none of above conditions
  <- -free;
     !!goto_depot.
  
  
// change to fetch gold goal

+!change_to_fetch(G)               // nothing to do
  :  .desire(fetch_gold(G)). 
+!change_to_fetch(G)               // drop goto_depot first
  :  .desire(goto_depot)
  <- .drop_desire(goto_depot); 
     !change_to_fetch(G).
+!change_to_fetch(G)               // change the gold to fetch
  :  .desire(fetch_gold(OtherG)) 
  <- .drop_desire(fetch_gold(OtherG)); 
     !change_to_fetch(G).
+!change_to_fetch(G)                // none of above conditions     
  <- -free;
     !!fetch_gold(G).  


// change to search gold goal
+!change_to_search     
  :  search_gold_strategy(S) 
  <- .print("New goal is find gold: ",S);
     -free;
     +free;
     .drop_all_desires;
     !!search_gold(S).

     
// also calculates the agent distance for a list of golds, 
// but considers other agents committed to the same gold and
// the agent fatigue
evaluate_golds([],[]) :- true.
evaluate_golds([gold(GX, GY)|R],[d(U,gold(GX,GY),Annot)|RD]) 
  :- evaluate_gold(gold(GX,GY),U,Annot) &  
     evaluate_golds(R,RD).
evaluate_golds([_|R],RD) 
  :- evaluate_golds(R,RD).

evaluate_gold(gold(X,Y),Utility,Annot)
  :- pos(AgX,AgY,_) & jia.path_length(AgX,AgY,X,Y,D) &
     jia.add_fatigue(D,Utility) &
     check_commit(gold(X,Y),Utility,Annot).

// distance 0, always consider
check_commit(_,0,in_my_place)   :- true.
// if no other is committed to the gold, OK.
check_commit(G,_,not_committed) :- not committed_to(G,_,_).
// if someone else if committed, check who is nearer  
check_commit(gold(X,Y),MyD,committed_by(Ag,at(OtX,OtY),far(OtD))) 
  :- committed_to(gold(X,Y),_,Ag) &          // get the agent committed to the gold
     jia.ag_pos(Ag,OtX,OtY) &                // get its location
     jia.path_length(OtX,OtY,X,Y,OtD) &      // calc its distance from the gold
     MyD < OtD.                              // ok to consider the gold if I am near
     

worthwhile(gold(_,_)) :- 
     carrying_gold(0).
worthwhile(gold(GX,GY)) :-  
     carrying_gold(NG) & NG > 0 &
     pos(AgX,AgY,Step) & 
     depot(_,DX,DY) &
     steps(_,TotalSteps) &
     AvailableSteps = TotalSteps - Step &

     // cost of fetching gold and after go to depot
     jia.add_fatigue(jia.path_length(AgX,AgY,GX,GY),NG,  CN4) & // ag to gold
     jia.add_fatigue(jia.path_length(GX,  GY,DX,DY),NG+1,CN5) & // go to depot
     
     AvailableSteps > (CN4 + CN5) * 1.1.


/* 
   Events handling
     . gold perception
     
   Note that perception (or communication) reaction should be
   atomic, so that their corresponding plans are handled 
   before the current goal intention.
*/



// I perceived unknown gold, decide next gold
@pcell0[atomic]          // atomic: so as not to handle another 
                         // event until handle gold is carrying on
+cell(X,Y,gold) 
  :  container_has_space &
     not gold(X,Y) // is is an unknown gold
  <- .print("Gold perceived: ",gold(X,Y));
     +gold(X,Y);
     !choose_goal.

// I am not free and do not have space, just add gold belief and announce to others
+cell(X,Y,gold) 
  :  not container_has_space & not gold(X,Y) & not committed(gold(X,Y),_,_)
  <- +gold(X,Y);
     +announced(gold(X,Y));
     .print("Announcing ",gold(X,Y)," to others");
     .broadcast(tell,gold(X,Y)). 

// If I see an empty cell where it was supposed to be gold, announce it to others
+cell(X,Y,empty)
  :  gold(X,Y) & 
     not .desire(fetch_gold(gold(X,Y))) // in this case, I empty the cell!
  <- !remove(gold(X,Y));
     .print("The gold at ",X,",",Y," was picked by someone else! Announcing to others.");
     .broadcast(tell,picked(gold(X,Y))). 

     
/* end of a simulation */

+end_of_simulation(S,R) 
  <- .drop_all_desires; 
     !remove(gold(_,_));
     .abolish(picked(_));

     -+search_gold_strategy(near_unvisited);
     .abolish(quadrant(X1,Y1,X2,Y2));
     .abolish(last_checked(_,_));

     -+free;
     
     .print("-- END ",S,": ",R).

+!remove(gold(X,Y))
  <- .abolish(gold(X,Y));
     .abolish(committed_to(gold(X,Y),_,_));
     .abolish(picked(gold(X,Y)));
     .abolish(announced(gold(X,Y)));
     .abolish(allocated(gold(X,Y),_)).

@rl[atomic]  
+restart 
  <- .print("*** Start it all again!"); 
     .drop_all_desires;
     !choose_goal.

