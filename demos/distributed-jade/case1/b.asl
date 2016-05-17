// Agent b in project testJadeDist.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true 
    <- .print("hello world.");
       .send(a,tell,hello).

