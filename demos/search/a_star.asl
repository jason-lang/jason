/* A* implementation */

{ register_function("search.h",2,"h") }

a_star( InitialState, Goal, Solution, Cost) :-
  .set.add_all(Closed,[]) &
  .queue.create(Open,priority) &
  .queue.add(Open,s(0,0,[op(initial,0)])) &
  a_star_l( Open, Goal, s(_,Cost,SolutionR), Closed) &
  .reverse(SolutionR,Solution).

a_star_l( Open, GoalState, s(F,G,[op(Op,GoalState)|Path]), Closed) :-
    .queue.head(Open,s(F,G,[op(Op,GoalState)|Path])).

a_star_l( Open, GoalState, Solution, Closed) :-
    .queue.remove(Open,s(F,G,[op(Op,State)|Path])) &
    //.print("exploring ",State," to be explored ", .length(Open)) &
    State \== GoalState &
    .findall(
	     s(NF,NG,[ op(NOp,NewState), op(Op,State)|Path]), // new paths
		   ( suc(State,NewState,Cost,NOp) &
         not .member(NewState, Closed) &
         .set.add(Closed, NewState) &
         NG = G + Cost & // cost to achieve NewState
         NF = search.h(NewState,GoalState) + NG // cost + heuristic for new state
       ),
       Suc
    ) &
    .queue.add_all(Open, Suc) &
    a_star_l( Open, GoalState, Solution, Closed).