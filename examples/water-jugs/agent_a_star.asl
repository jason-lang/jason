// problem description:
//     https://www.eecis.udel.edu/~mccoy/courses/cisc4-681.10f/lec-materials/handouts/search-water-jug-handout.pdf
//
// state is a pair:
//     [water in 5l jug, water in 3l jug]

!jugs(_,1). // initial goal


+!jugs(J5,J3) 
   <- ?a_star( [0,0], [J5,J3], [_|Solution], Cost);
      .print("solution A* =", Solution, " with ",Cost," liters of water.");
      .print("Executing the plan...");
      for ( .member( op(Op,St),Solution)) {
         Op; // execute the actions
      }
      //.stopMAS;
   .

+jug(_,_) : jug(5,J5) & jug(3,J3) <- .print("           State: (",J5,",",J3,")").

/* The following two rules (suc/4 and h3) are domain dependent
   and have to be redefined for this problem */

// sucessor definition: suc(CurrentState,NewState,Cost,Operation)
suc([J5,J3],[5,J3],5,fill(5)). // cost is based on spent water
suc([J5,J3],[J5,3],3,fill(3)).
suc([J5,J3],[0,J3],0,empty(5)).
suc([J5,J3],[J5,0],0,empty(3)).
suc([J5,J3],[NJ5,NJ3],0,pour(5,3)) :-
    J5 > 0 & J3 < 3 &
    V = math.min(3-J3,J5) &
    NJ5 = J5 - V & NJ3 = J3 + V.
suc([J5,J3],[NJ5,NJ3],0,pour(3,5)) :-
    J5 < 5 & J3 > 0 &
    V = math.min(5-J5,J3) &
    NJ5 = J5 + V & NJ3 = J3 - V.

// heuristic definition: h(CurrentState,Goal,H)
h(State,Goal,0). // no heuristic indeed!

{ include("a_star.asl") }
