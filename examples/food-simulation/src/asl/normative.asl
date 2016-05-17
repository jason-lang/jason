/* -- normative agent -- */

// Rule-n1: only eat food that is free or allocated to me
+step(_) : food(X,Y,my_pos,A) & (pos(A,_,_) | A == -1) <- eat.

// Rule-n2: if I see food allocated to me, move to there
+step(_) : food(X,Y,see,Me)  & pos(Me,_,_) & not agent(_,X,Y,_,_) <- move(X,Y).

// Rule-n3: if I see unallocated food, move to there
+step(_) : food(X,Y,see,-1)  & not agent(_,X,Y,_,_)               <- move(X,Y).

// Rule-n4: if I see another agent eating food owned by others, attack it
+step(_) : food(X,Y,see,OAg) & agent(AgId,X,Y,_,eating) & 
           AgId \== OAg & OAg \== -1 
        <- attack(X,Y).

// Rule-n5: if I smell my food, move to there
+step(_) : food(X,Y,smell,Me) & pos(Me,_,_) <- move(X,Y).

// Rule-n6: if I smell unallocated food, move to there
+step(_) : food(X,Y,smell,-1)               <- move(X,Y).

// Rule-n7: otherwise, move randomly
+step(_) <- random_move.
