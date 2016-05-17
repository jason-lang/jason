// An agent for the Block World
// This ones tries to be closer to a GOAL program

/* Initial beliefs and rules */
clear(table).
clear(X) :- not(on(Y,X)).
tower([X]) :- on(X,table).
tower([X,Y|T]) :- on(X,Y) & tower([Y|T]).

allowed(X,Y) :- clear(X) & clear(Y) & not on(X,Y).

// an ad-hoc implementation of the GOAL "a_goal" construct for this problem
a_goal(S,G) :- .member(S1,S) & .suffix(G,S1).

/* Initial goals */
!state([[a,e,b],[f,d,c],[g]]).

/* Plans */
+!state(S)
  :  a_goal(S,[X,Y|T]) & tower([Y|T]) & allowed(X,Y)
  <- move(X,Y); !state(S).
+!state(S)
  :  a_goal(S,[X|T]) & not tower([X|T]) & allowed(X,table)
  <- move(X,table); !state(S).
+!state(S).

