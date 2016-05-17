// An agent for the Blocks World
// It tries first to build the stacks that are closer to completion

/* Initial beliefs and rules */
clear(table).
clear(X) :- not(on(Y,X)).
tower([X]) :- on(X,table).
tower([X,Y|T]) :- on(X,Y) & tower([Y|T]).

highest_partial_tower(L,[]) :- calc(L,[]).
highest_partial_tower(L,T) :- calc(L,L1) & .sort(L1,L2) & .reverse(L2,[s(_,T)|T3]).

calc([],[]).
calc([H|T],TR) :- ready(H,S) & S=.length(H) & calc(T,TR).
calc([H|T],[s(S,H)|TR]) :- ready(H,S) & calc(T,TR).

ready([],0).
ready(L,S) :- tower(L) & S=.length(L).
ready([H|T],S) :- ready(T,S).

/* Initial goals */
!state2([[a,e,b],[f,d,c],[g]]).

/* Plans */
+!state2(S) : highest_partial_tower(S,[]).
+!state2(S) : highest_partial_tower(S,T) <- !tower(T); !state2(S).

+!tower(T) : tower(T).
+!tower([T]) <- !on(T,table).
+!tower([X,Y|T]) <- !tower([Y|T]); !on(X,Y).

+!on(X,Y) : on(X,Y).
+!on(X,Y) <- !clear(X); !clear(Y); move(X,Y).

+!clear(X) : clear(X).
+!clear(X) : tower([H|T]) & .member(X,T)
  <- move(H,table);
     !clear(X).

