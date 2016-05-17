// Agent a in project testJadeDist.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true <- .print("hello world.").

+hello[source(A)] <- .print("I received a hello from ",A).
