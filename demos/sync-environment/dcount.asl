// this agent has TWO intentions to act

vl(0).

!i1.
!i2.


/* Plans */

+!i1 : vl(500) <- !reset.
+!i1  <- act; // do some action
         !inc_vl;
         !!i1.

+!i2 : vl(500) <- !reset.
+!i2  <- act; // do some action
         !inc_vl;
         !!i2.


@l[atomic]
+!inc_vl
   <- ?vl(X);
      screen.show_counter(X); // show counter
      -+vl(X+1).


@r[atomic]
+!reset : vl(_)
   <- .drop_all_desires;
      -+vl(1); // add 1 since I am killing at one intentoin waiting act
      !i1;
      !i2.

//+executed_actions(X) : X mod 9 \== 0 <- .println("Problem! Error=", X mod 9).

