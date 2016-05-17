// An agent for the Blocks World
// Tries each stack of the goal world concurrently

/* Initial beliefs and rules */
clear(table).
clear(X) :- not(on(Y,X)).
tower([X]) :- on(X,table).
tower([X,Y|T]) :- on(X,Y) & tower([Y|T]).

/* Initial goals */
// Try each stack concurrently
!tower([g]).
!tower([f,d,c]).
!tower([a,e,b]).

/* Plans */

+!tower(T) : tower(T).
+!tower([T]) <- !on(T,table).
+!tower([X,Y|T]) <- !tower([Y|T]); !on(X,Y).

+!on(X,Y) : on(X,Y).
@po[atomic] // don't allow concurrency within each "on(X,Y)" subgoal
+!on(X,Y) <- !clear(X); !clear(Y); move(X,Y).

+!clear(X) : clear(X).
+!clear(X) : tower([H|T]) & .member(X,T)
  <- move(H,table);
     !clear(X).

