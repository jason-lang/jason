!start. // initial goal

+!start : true
   <- .print("Creating agent");
      .create_agent(bob, "bob.asl");
      .send(bob, achieve, a);
      .wait(100);
      .print("Killing agent bob!");
      .kill_agent(bob);

      .print("Creating agent with internal action");
      mylib.my_create_ag;

      !end(10000).

+!end(T) : T <= 0 <- .stopMAS.
+!end(T) : true
   <- .print("The MAS will stop in ",T/1000," seconds!");
      .wait(2000); !end(T-2000).
