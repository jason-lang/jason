/* -- strategic agent -- */

// food in my position
+step(_) : food(X,Y,my_pos,_) <- eat.

// food I see
+step(_) : food(X,Y,see,_) & not agent(_,X,Y,_,_) <- move(X,Y).
+step(_) : food(X,Y,see,_) & agent(_,X,Y,S,eating) & strength(MS) & MS > S <- attack(X,Y).

// food I smell
+step(_) : food(X,Y,smell,_) <- move(X,Y). 

// nothing else
+step(_) <- random_move.
