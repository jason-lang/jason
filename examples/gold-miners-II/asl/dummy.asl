// Code of dummy agents (Blue team)

// the following plans (+pos....) react to the starting step
// (since each new step causes a new +pos perception)

/* -- useful rules */

// whether to go to depot
go_depot :- carrying_gold(3).
go_depot :- carrying_gold(N) & N > 0 & pos(_,_,Step) & steps(_,NSteps) & Step+200 > NSteps.

// find a free random location
random_pos(X,Y) :-
   pos(AgX,AgY,_) &
   jia.random(RX,20)   & X = (RX-10)+AgX & X > 0 &
   jia.random(RY,20,5) & Y = (RY-10)+AgY &
   not jia.obstacle(X,Y).


/* -- Gold found! -- */

// in the position of the agent
+pos(X,Y,_)
   : cell(X,Y,gold) &
     carrying_gold(N) & N < 3 // container has space
  <- do(pick);
     .print("picked gold!");
     -+back_pos(X,Y). // remembers a place to return

// in a cell besides
+pos(X,Y,_)
   : cell(GX,GY,gold) &
     carrying_gold(N) & N < 3 // container has space
  <- jia.direction(X, Y, GX, GY, D);
     do(D).

/* -- has gold, carry it/them to depot -- */

// when arrive on depot
+pos(X,Y,_)
   : go_depot &
     depot(_,X,Y)
  <- .print("in depot");
     do(drop).

// when still not in depot
+pos(X,Y,_)
   : go_depot &
     depot(_,DX,DY)
  <- jia.direction(X, Y, DX, DY, D); // uses A* to find a path to the depot
     //.print("from ",X,"x",Y," to ",DX,"x",DY," -> ",D);
     do(D).

/* -- go to the back pos -- */

// at the back_pos
+pos(X,Y,_)
   : back_pos(X,Y) | // I am at back pos, find another
     (back_pos(BX,BY) & jia.direction(X, Y, BX, BY, skip)) // impossible to go to back_pos, find another
  <- !define_new_pos.
+pos(X,Y,_)
   : back_pos(BX,BY) & jia.direction(X, Y, BX, BY, D) // one step towards back_pos
  <- do(D).

/* -- random move -- */
+pos(_,_,_)
   <- !define_new_pos.

+!define_new_pos
   <- ?pos(X,Y,_);
      ?random_pos(NX,NY);
     //.print("New point ",NX,",",NY);
      -+back_pos(NX,NY);
      jia.direction(X, Y, NX, NY, D);
      do(D).


