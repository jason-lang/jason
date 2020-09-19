// problem description:
//     https://www.eecis.udel.edu/~mccoy/courses/cisc4-681.10f/lec-materials/handouts/search-water-jug-handout.pdf
//
// state is a pair:
//     [water in 4l jug, water in 3l jug]

!solve.
+!solve
   <- InitRule = system.time;
      ?a_star( [0,0], [2,0], Solution, Cost);
      .print("solution A* =", Solution, " with cost ",Cost," in ", (system.time - InitRule), " ms.");
      .stopMAS;
   .

/* The following two rules are domain dependent and have to be redefined accordingly */

// sucessor definition: suc(CurrentState,NewState,Cost,Operation)
suc([J4,J3],[4,J3],4+1,fill4j). // to fill costs more (the water cost)
suc([J4,J3],[J4,3],3+1,fill3j).
suc([J4,J3],[0,J3],1,empty4j).
suc([J4,J3],[J4,0],1,empty3j).
suc([J4,J3],[NJ4,NJ3],1,pour4to3) :-
    J4 > 0 & J3 < 3 &
    V = math.min(3-J3,J4) &
    NJ4 = J4 - V & NJ3 = J3 + V.
suc([J4,J3],[NJ4,NJ3],1,pour3to4) :-
    J4 < 4 & J3 > 0 &
    V = math.min(4-J4,J3) &
    NJ4 = J4 + V & NJ3 = J3 - V.

// heutistic definition: h(CurrentState,Goal,H)
h(State,Goal,0). // no heuristic indeed!

{ include("a_star.asl") }
