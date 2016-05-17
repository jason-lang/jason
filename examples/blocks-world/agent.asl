// An agent for the Blocks World

/* Initial beliefs and rules */
clear(table).
clear(X) :- not(on(_,X)).
tower([X]) :- on(X,table).
tower([X,Y|T]) :- on(X,Y) & tower([Y|T]).

/* Initial goals */
// The final state to be achieved
// (note that this only makes sense in applications such as the Blocks World)
!state([[a,e,b],[f,d,c],[g]]).

/* Plans */
// Achieve one tower at a time
+!state([])    <- .print("Finished!").
+!state([H|T]) <- !tower(H); !state(T).

// Achieve a state where a tower is built
+!tower(T) : tower(T). // already achieved, nothing else to do
+!tower([T])     <- !on(T,table). // easy to do a singleton tower
+!tower([X,Y|T]) <- !tower([Y|T]); !on(X,Y). // break my problem into smaller ones

// Achieve a state of affairs where I believe a block is on top of another
+!on(X,Y) : on(X,Y). // already achieved
+!on(X,Y) <- !clear(X); !clear(Y); move(X,Y). // make sure blocks are clear, then move

// Achieve a state of affairs where block X is clear (i.e.e, at the top of a tower)
+!clear(X) : clear(X). // already achieved
// remove top block when the block I need to clear is underneath it
+!clear(X)
  :  tower([H|T]) & .member(X,T)
  <- move(H,table);
     !clear(X).  // carry on trying, until achieved

