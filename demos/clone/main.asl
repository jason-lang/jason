// Agent main in project clone.mas2j

/* Initial beliefs and rules */

test(1).

/* Initial goals */

!start.

/* Plans */

+!start 
   <- +a(40); 
      .clone("bob");
      .send(bob,tell,p(10)).
      
+p(X) : a(V) <- .print(X," : ",V).

