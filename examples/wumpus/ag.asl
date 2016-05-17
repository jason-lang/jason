// Agent ag in project wumpus.mas2j

{ include("kb.asl") } 
{ include("search.asl") } 
{ include("aux.asl") } 


/* Initial beliefs and rules */

pos(1,1).            // my initial location
orientation(east).   // and orientation 

// scenario borders
// borders(BottomLeftX, BottomLeftY, TopRightX, TopRightY) 
borders(1, 1, 4, 4). // for R&N


/* Initial goals */

!main.

/* Plans */

+!main
   <- !init;
      !find(gold);
      !quit_cave.
          
+!init 
   <- !update(breeze); // update perception for location 1,1
      !update(stench).    

+!find(gold) : has_gold.
+!find(gold) 
   <- !explore;
      !find(gold).

+!quit_cave
   <- .print("I am leaving the cave!!!");
      !goto(1,1);
      climb.

      
// TODO: find near safe and unvisited location    
+!explore
    : borders(BottomLeftX, BottomLeftY, TopRightX, TopRightY) &
      .range(X,BottomLeftX,TopRightX) &
      .range(Y,BottomLeftY,TopRightY) &
      not visited(X,Y) &
      safe(X,Y)
   <- .print("going to ",X,",",Y);
      !goto(X,Y).
+!explore
   <- .print("no safe place to explore!").

// TODO: shot wumpus if his location is known 
// TODO: if no safe location is known, select the lest risky one to go

+glitter 
   <- grab;
      +has_gold;
      .succeed_goal(explore). // to stop exploring

+!test // solution for R&N scenario
   <- !init;
      !goto(2,1);
      !goto(1,2);
      !goto(2,2);
      !goto(2,3); 
      !show_info;
      !quit_cave.


+!goto(X,Y) : pos(X,Y).
+!goto(X,Y) 
    : pos(MyX,MyY) & orientation(O) &
      search( [p(0,[s(MyX,MyY,O,no)],[])], s(X,Y,_,_), [Action|_]) 
   <- .print("doing ",Action);
      .wait(500);
      !do(Action);
      !goto(X,Y).


// perform action forward and update beliefs accordingly
+!do(forward)
   <- forward;
      ?pos(X,Y);
      ?orientation(O);
      
      if (bump) {
         +wall(X,Y)
      } else {
         !update(pos(X,Y),O);
         !update(breeze);
         !update(stench);
      }.

// perform action turn and update beliefs accordingly     
+!do(turn(D))
   <- turn(D); 
      ?orientation(O);
      !update(orientation(O),D).

// update beliefs using a coordinate system

+!update(pos(X,Y), O) : next_state( s(X,Y,O,_), forward, s(NX,NY,_,_)) <- -+ pos(NX,NY). // next_state is define in search.asl

+!update(breeze) : not breeze[source(percept)] & pos(X,Y) <- +~breeze(X,Y). 
+!update(breeze) :     breeze[source(percept)] & pos(X,Y) <- +breeze(X,Y).  

+!update(stench) : not stench[source(percept)] & pos(X,Y) <- +~stench(X,Y). 
+!update(stench) :     stench[source(percept)] & pos(X,Y) <- +stench(X,Y).  

+!update(orientation(Old),D) : next_state( s(_,_,Old,_),  turn(D), s(_,_,New,_)) <- -+ orientation(New). // next_state is define in search.asl
