/* A* implementation */

{ register_function("search.h",2,"h") } // register the predicate h/3 as a function search.h(state s, goal g) = heuristic of s (for g)

a_star( InitialState, Goal, Solution, Cost) :-
  .set.add_all(Closed,[InitialState]) &
  .queue.create(Open,priority) &
  .queue.add(Open,s(0,0,[op(initial,InitialState)])) &
  a_star_l( Open, Goal, s(_,Cost,SolutionR), Closed) &
  .reverse(SolutionR,Solution).

// goal found in the first state of the open queue, stop searching
a_star_l( Open, GoalState, s(F,G,[op(Op,GoalState)|Path]), Closed) :-
    .queue.head(Open,s(F,G,[op(Op,GoalState)|Path])).

// first state in open queue is not a goal, explore it
a_star_l( Open, GoalState, Solution, Closed) :-
    .queue.remove(Open,s(F,G,[op(Op,State)|Path])) &
    //.print("exploring ",State," to be explored ", .length(Open)) &
    State \== GoalState &
    .set.add(Closed, State) & // do not explore State again
    .findall(
         s(NF,NG,[ op(NOp,NewState), op(Op,State)|Path]), // new paths
           ( suc(State,NewState,Cost,NOp) &
             not .member(NewState, Closed) &
             NG = G + Cost & // cost to achieve NewState
             NF = search.h(NewState,GoalState) + NG // cost + heuristic for new state
           ),
         Suc
    ) &
    //.print("     ",Suc) &
    .queue.add_all(Open, Suc) &
    a_star_l( Open, GoalState, Solution, Closed).
