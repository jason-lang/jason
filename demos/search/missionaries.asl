// problem description:
//     http://www.aiai.ed.ac.uk/~gwickler/missionaries.html
//
// state is a pair:
//     [#missionaires at left, #cannibals at left, boat side]

!solve.
+!solve
   <- InitRule = system.time;
      ?a_star( [3,3,l], [0,0,r], Solution, Cost);
      .print("solution A*");
      for (.member( S, Solution)) {
        .print("   ",S);
      }
   .

/* The following two rules are domain dependent and have to be redefined accordingly */

// sucessor definition: suc(CurrentState,NewState,Cost,Operation)
suc([M,C,l],[NM,C,r],1,move2mis_LR) :- M>=2 & NM=M-2 & ok(NM,C).
suc([M,C,l],[NM,C,r],1,move1mis_LR) :- M>=1 & NM=M-1 & ok(NM,C).
suc([M,C,r],[NM,C,l],1,move2mis_RL) :- M<=1 & NM=M+2 & ok(NM,C).
suc([M,C,r],[NM,C,l],1,move1mis_RL) :- M<=2 & NM=M+1 & ok(NM,C).

suc([M,C,l],[M,NC,r],1,move2can_LR) :- C>=2 & NC=C-2 & ok(M,NC).
suc([M,C,l],[M,NC,r],1,move1can_LR) :- C>=1 & NC=C-1 & ok(M,NC).
suc([M,C,r],[M,NC,l],1,move2can_RL) :- C<=1 & NC=C+2 & ok(M,NC).
suc([M,C,r],[M,NC,l],1,move1can_RL) :- C<=2 & NC=C+1 & ok(M,NC).

suc([M,C,l],[NM,NC,r],1,move1can1mis_LR) :- M>0 & C>0 & NM=M-1 & NC=C-1 & ok(NM,NC).
suc([M,C,r],[NM,NC,l],1,move1can1mis_RL) :- M<3 & C<3 & NM=M+1 & NC=C+1 & ok(NM,NC).

ok(3,_). // all missionaries at left -> ok.
ok(0,_). // all missionaries at right -> ok.
ok(M,C) :- M >= C & 3-M >= 3-C.

// heutistic definition: h(CurrentState,Goal,H)
h(State,Goal,0). // no heuristic indeed!

{ include("a_star.asl") }
