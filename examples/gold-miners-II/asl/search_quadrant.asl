// find gold by scanning a quadrant

/* plans for sending the initial position to leader */

// get my quadrant: change find gold strategy
+quadrant(X1,Y1,X2,Y2)
  <- .print("I've got my quadrant, changing search gold strategy.");
     -+search_gold_strategy(quadrant);
     !!choose_goal.
          
+!search_gold(quadrant) : last_checked(X,Y)     <- !prep_around(X,Y).
+!search_gold(quadrant) : quadrant(X1,Y1,X2,Y2) <- !prep_around(X1,Y1).
+!search_gold(quadrant) <- .print("search by quadrant again"); .wait(1000); !!search_gold(quadrant).
-!search_gold(quadrant) <- !!choose_goal.

// if I am around upper-left corner, move to upper-right corner
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
     do(down);
     do(down);
     do(down);
     !prep_around(X1,YF).

// if I am around the left side, move to right side two lines bellow
+around(X1,Y) : quadrant(X1,Y1,X2,Y2) & free  
  <- ?calc_new_y(Y,Y2,YF);
     .print("in Q3 to ", X2, "x", YF); 
     do(down);
     do(down);
     do(down);
     !prep_around(X2,YF).

// the last "around" was not any of above, go back to my quadrant
+around(X,Y) : quadrant(X1,Y1,X2,Y2) & free & Y <= Y2 & Y >= Y1  
  <- .print("in no Q, going to X1");
     !prep_around(X1,Y).
+around(X,Y) : quadrant(X1,Y1,X2,Y2) & free & X <= X2 & X >= X1  
  <- .print("in no Q, going to Y1");
     !prep_around(X,Y1).

+around(X,Y) : quadrant(X1,Y1,X2,Y2)
  <- .print("It should never happen!!!!!! - go home");
     !prep_around(X1,Y1).

// next line is the bottom line of the quadrant
// if 3 lines bellow is too far
calc_new_y(AgY,QuadY2,QuadY2) :- AgY+3 > QuadY2.

// otherwise, the next line is 3 lines bellow
calc_new_y(AgY,_,Y) :- Y = AgY+3.
     
+!prep_around(X,Y) : free
  <- -around(_,_); -last_dir(_); !around(X,Y).
     
+!around(X,Y) 
   :  // I am around to some location 
      // and I am near it or
      // the last action was skip (meaning that there is no path to there)
      (pos(AgX,AgY,_) & jia.neighbour(AgX,AgY,X,Y)) | last_dir(skip) 
   <- +around(X,Y).
+!around(X,Y) : not around(X,Y) & free
   <- jia.set_target(X,Y);
      !next_step(X,Y);
      !!around(X,Y).
+!around(X,Y) : free 
   <- !!around(X,Y).

@lfq[atomic]
-free 
  :  .desire(around(_,_)) |.desire(prep_around(_,_))    
  <- .print("Dropping around(_,_) desires and intentions to handle gold");
     .drop_desire(around(_,_));
     .drop_desire(prep_around(_,_));
     ?pos(X,Y,_);
     -+last_checked(X,Y).
     
