
/* A* implementation to find paths */

// search(OpenStates, Goal, Solution)
// each element of OpenStates is a path p(F value,[current state, previous states...],[last action, previous actions])
// a state is s(X,Y,O,G), X,Y are location, O is orientation, G is true if carrying gold

{ register_function("search.h",2,"h") }

search( [p(F,[GoalState|Path],Actions) | _], GoalState, Solution) :-
   .reverse(Actions,Solution).

search( [p(F,[State|Path],PrevActions)|Open], GoalState, Solution) :-
   State \== GoalState & //.print("  ** ",State," ",PrevActions) & 
   .findall(
        p(NewF,[NewState,State|Path],[Action|PrevActions]), // new paths 
        ( next_state(State,Action,NewState) & 
          not .member(NewState, [State|Path]) & 
          NewF = search.h(NewState,GoalState) + .length(PrevActions) + 1
        ), 
        Sucs) &
   .concat(Open,Sucs,LT) & 
   .sort(LT,NewOpen) &  // sort by F (H+G), so A*
   //.print("open nodes #",.length(NewOpen)) & //.print(" new open = ",NewOpen) & 
   search( NewOpen, GoalState, Solution).

// heuristic (distance to target + orientation towards the target)
h( s(X,Y,O,G), s(TargetX,TargetY,_,_), H ) :-
   dist(X,Y,TargetX,TargetY,D) & 
   orientation_to_target(h,X,TargetX,O,CostOX) &
   orientation_to_target(v,Y,TargetY,O,CostOY) &
   H = D + CostOX + CostOY.

dist(X1,Y1,X2,Y2,D) :- D = math.abs(X1-X2) + math.abs(Y1-Y2).

orientation_to_target(_,X,X,_,0). // I am in the target
orientation_to_target(h,X,Target,east,0)  :- Target > X. // I am turned to the target
orientation_to_target(h,X,Target,west,0)  :- Target < X.
orientation_to_target(v,Y,Target,north,0) :- Target < Y.
orientation_to_target(v,Y,Target,south,0) :- Target > Y.
orientation_to_target(_,_,_,_,1).


// simulation of what will be the next state in case of any possible action
next_state( s(X,Y,east,G),   forward, s(X+1,Y,east,G))  :- not wall(X+1,Y) & not out_of_grid(X+1,Y) & safe(X+1,Y).
next_state( s(X,Y,west,G),   forward, s(X-1,Y,west,G))  :- not wall(X-1,Y) & not out_of_grid(X-1,Y) & safe(X-1,Y).
next_state( s(X,Y,north,G),  forward, s(X,Y+1,north,G)) :- not wall(X,Y+1) & not out_of_grid(X,Y+1) & safe(X,Y+1).
next_state( s(X,Y,south,G),  forward, s(X,Y-1,south,G)) :- not wall(X,Y-1) & not out_of_grid(X,Y-1) & safe(X,Y-1).

next_state( s(X,Y,east,G),  turn(left), s(X,Y,north,G)).
next_state( s(X,Y,north,G), turn(left), s(X,Y,west,G)).
next_state( s(X,Y,west,G),  turn(left), s(X,Y,south,G)).
next_state( s(X,Y,south,G), turn(left), s(X,Y,east,G)).

next_state( s(X,Y,OldOr,G),  turn(right), s(X,Y,NewOr,G)) :- next_state( s(X,Y,NewOr,G), turn(left), s(X,Y,OldOr,G)).

// OLD code

/* // simple implementation of Depth-first searchto find paths
search( [p([GoalState|Path],Actions) | _], GoalState, p([GoalState|Path],Actions)).

search( [p([State|Path],PrevActions)|Open], GoalState, Solution) :-
   State \== GoalState & 
   .findall(
        p([NewState, State|Path],[Action|PrevActions]), // new paths 
        ( next_state(State,Action,NewState) & not .member(NewState, [State|Path])), 
        Suc) &
   .concat( Open, Suc, NewOpen) &
   search( NewOpen, GoalState, Solution).

*/

/*
// a insertion-sort algorithm used to include new states in open
// the order is defined by f, f = g + h
// g is the depth, h is the expected distance + right orientation
insert_all([], L, L, _).
insert_all([H|T],L1,L2,Goal) :- insert(H,L1,L1H,Goal) & insert_all(T,L1H,L2,Goal).

insert(P,[],    [P],     _).
insert(P,[A|L], [P,A|L], Goal) :- better(P,A,Goal).
insert(P,[A|L], [A|L2],  Goal) :- insert(P,L,L2,Goal). 
   
//better(P1, P2,Goal) :- f(P1,F1,Goal) & f(P2,F2,Goal) & F1 < F2.

better(p(F1,_,_), p(F2,_,_),Goal) :- F1 < F2.
*/

