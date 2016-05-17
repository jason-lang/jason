/* -- blind agent -- */

// rule 1: If there is food in my cell, eat.
+step(_) : food(X,Y,my_pos,_) <- eat.

// rule 2: If I see food in a free cell, move to there
+step(_) : food(X,Y,see,_) & not agent(_,X,Y,_,_) <- move(X,Y).

// rule 3: If I see food in an occupied cell, attack the agent at that cell
+step(_) : food(X,Y,see,_) & agent(_,X,Y,_,eating) <- attack(X,Y).

// rule 4: If I smell food, move to there
+step(_) : food(X,Y,smell,_) <- move(X,Y). 

// rule 5: Otherwise, do a random movement
+step(_) <- random_move.
