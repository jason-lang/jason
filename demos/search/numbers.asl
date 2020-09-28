!test.
+!test
   <- InitRule = system.time;
      ?a_star( 0, 9, Solution, Cost);
      .print("solution A* =", Solution, " with cost ",Cost," in ", (system.time - InitRule), " ms.");
   .

/* The following two rules are domain dependent and have to be redefined accordingly */

// sucessor definition: suc(CurrentState,NewState,Cost,Operation)
suc(C,C2,1,add2) :- C2 = C + 2.
suc(C,C1,1,add1) :- C1 = C + 1.

// heutistic definition: h(CurrentState,Goal,H)
h(State,Goal,H) :- H = math.abs(Goal - State). // very simple heuristic

{ include("a_star.asl") }
