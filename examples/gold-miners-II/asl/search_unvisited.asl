// find gold by going to the near least unvisited location

+!search_gold(near_unvisited)
   :  pos(X,Y,_) & free & 
      jia.near_least_visited(X,Y,ToX,ToY)
   <- !pos(ToX,ToY);
      !!search_gold(near_unvisited).

+!search_gold(near_unvisited) : free
   <- !!search_gold(near_unvisited).

-!search_gold(near_unvisited)
   <- !!choose_goal.

@lfg[atomic]   
-free 
  :  .desire(search_gold(_))
  <- .print("Dropping search_gold desires and intentions to handle gold");
     .drop_desire(search_gold(_)).

