/* -- reputation agent -- */

// Rule-r1: eat food that is either allocate to me, free, or  belongs to a cheater
+step(_) : food(_,_,my_pos,A) & (pos(A,_,_) | A == -1 | cheater(A)) <- eat.

// Rule-r2: if I see food allocated to me, move to there
+step(_) : food(X,Y,see,Me)  & pos(Me,_,_) & not agent(_,X,Y,_,_) <- move(X,Y).

// Rule-r3: if I see unallocated food, move to there
+step(_) : food(X,Y,see,-1)  & not agent(_,X,Y,_,_)               <- move(X,Y).

// Rule-r4: attack cheaters that are weaker than me
+step(_) : food(X,Y,see,_)   & agent(AgId,X,Y,S,eating) &
           cheater(AgId) &
           strength(MS) & MS > S
        <- attack(X,Y).


// Rule-r5: if I smell my food, move to there
+step(_) : food(X,Y,smell,Me) & pos(Me,_,_) <- move(X,Y).

// Rule-r6: if I smell unallocated food, move to there
+step(_) : food(X,Y,smell,-1)               <- move(X,Y).

// Rule-r7: otherwise, move randomly
+step(_) <- random_move.


// reputation model

+attacked(A,_) <- +cheater(A); .broadcast(tell, cheater(A)).

//+new_cheater(A,N) <- +cheater(A)[name(N)]; -new_cheater(A,N)[source(_)].

