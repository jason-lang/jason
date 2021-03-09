// Agent a in project function.mas2j


/* define a new function in Java */
{ register_function("myf.sin") } // Register an user defined function
                                 // the code of this function is in the
                                 // class myf.sin (see sin.java for more
                                 // info)

/* define a new function in AgentSpeak */

{ register_function("myf.sum",2,"sum") }
                                 // Register an user defined function
                                 // myf.sum is the name of the function
                                 // 2 is its arity
                                 // sum is the name of predicate, that
                                 // should have arity = 3
                                 //
                                 // the code of this function is in the
                                 // rule sum below, the last argument
                                 // is the return of the function
sum(X,Y,S) :- S = X + Y.

{ register_function("myf.limit", 0,"limit") } // example of constant function
limit(10).


/* Initial beliefs (used to show the use of .count) */
b(10).
b(20).
t(x).

/* Initial goals */

!show_predef_function.
!show_userdef_function.

/* Plans */

+!show_predef_function
   <- X = math.max(4, math.abs(-10));
      .print("Max=",X);
      .print("Max=",math.max(4, math.abs(-10)));
      .print("Number of b/1 beliefs=", .count(b(_))).

+!show_userdef_function
   <- .print("Sin of 90   =", myf.sin(90));
      .print("1+2         =", myf.sum(1,2));
      .print("limit       =", myf.limit);
      .print("5+(2+limit) =", myf.sum(5,myf.sum(2,myf.limit))).

