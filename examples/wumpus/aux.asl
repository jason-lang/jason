// some plans for the testing phase (not used in the final relase)

/* perception simulation */
/*~breeze(1,1).
~stench(1,1).

breeze(2,1).
~stench(2,1).

~breeze(1,2).
stench(1,2).

~breeze(2,2).
~stench(2,2).

breeze(2,3).
stench(2,3).

breeze(3,2).
~stench(3,2).*/


+!show_info  <-
   for (.member(X, [1,2,3,4])) {
       for (.member(Y, [1,2,3,4])) {
         !safe(X,Y);
         !pit(X,Y);
         !wumpus(X,Y);
         if (safe(X,Y) & not visited(X,Y)) {
            .print("**")
         }
         if (might_be_pit(X,Y) & not pit(X,Y) & not ~pit(X,Y)) {
            .print("   ??",X,",",Y)
         }
      }
   }.
   
+!safe(X,Y) : safe(X,Y) <- .print(safe(X,Y)).
+!safe(X,Y)             <- .print("   ",nsafe(X,Y)).

+!pit(X,Y) : pit(X,Y) <- .print(pit(X,Y)).
+!pit(X,Y)            <- .print("   ",npit(X,Y)).

+!wumpus(X,Y) : wumpus(X,Y) <- .print(wumpus(X,Y)).
+!wumpus(X,Y)               <- .print("   ",nwumpus(X,Y)).

