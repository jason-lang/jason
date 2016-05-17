// An agent for the Blocks World
// This version uses plan patterns for declarative goals

/* Initial beliefs and rules */
clear(table).
clear(X) :- not(on(Y,X)).
tower([X]) :- on(X,table).
tower([X,Y|T]) :- on(X,Y) & tower([Y|T]).

/* Initial goals */
// The final state to be achieved
!state([[a,e,b],[f,d,c],[g]]).

/* Plans */
// Achieve one tower at a time
+!state([]).
+!state([H|T]) <- !tower(H); !state(T).

{begin dg(tower(T))} // plans for a declarative goal (dg)
+!tower([T]) <- !on(T,table).
+!tower([X,Y|T]) <- !tower([Y|T]); !on(X,Y).
{end}

{begin dg(on(X,Y))}
+!on(X,Y) <- !clear(X); !clear(Y); move(X,Y).
{end}

{begin bc(clear(X),dg(clear(X)))} // blindly committed (bc) to this goal
+!clear(X)
  :  tower([H|T]) & .member(X,T)
  <- move(H,table).
{end}

