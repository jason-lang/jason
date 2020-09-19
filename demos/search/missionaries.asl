// problem description:
//     http://www.aiai.ed.ac.uk/~gwickler/missionaries.html
//
// state is a triple:
//     [#missionaires at left, #cannibals at left, boat side]

!solve.
+!solve
   <- ?a_star( [3,3,l], [0,0,r], Solution, Cost);
      for (.member( op(O,S), Solution)) {
        .print("   ",S," <-< ",O);
      }
   .

// sucessor definition: suc(CurrentState,NewState,Cost,Operation)
suc([M,C,l],[NM,C,r],1,move2mis_LR) :- NM=M-2 & ok(NM,C).
suc([M,C,l],[NM,C,r],1,move1mis_LR) :- NM=M-1 & ok(NM,C).
suc([M,C,r],[NM,C,l],1,move2mis_RL) :- NM=M+2 & ok(NM,C).
suc([M,C,r],[NM,C,l],1,move1mis_RL) :- NM=M+1 & ok(NM,C).

suc([M,C,l],[M,NC,r],1,move2can_LR) :- NC=C-2 & ok(M,NC).
suc([M,C,l],[M,NC,r],1,move1can_LR) :- NC=C-1 & ok(M,NC).
suc([M,C,r],[M,NC,l],1,move2can_RL) :- NC=C+2 & ok(M,NC).
suc([M,C,r],[M,NC,l],1,move1can_RL) :- NC=C+1 & ok(M,NC).

suc([M,C,l],[NM,NC,r],1,move1can1mis_LR) :- NM=M-1 & NC=C-1 & ok(NM,NC).
suc([M,C,r],[NM,NC,l],1,move1can1mis_RL) :- NM=M+1 & NC=C+1 & ok(NM,NC).

ok(M,C) :-
    M >= 0 & M <= 3 &
    C >= 0 & C <= 3 &
    (M >= C | M == 0) &
    (3-M >= 3-C | 3-M == 0).

// heutistic definition: h(CurrentState,Goal,H)
h(State,Goal,0). // no heuristic indeed!

{ include("a_star.asl") }
