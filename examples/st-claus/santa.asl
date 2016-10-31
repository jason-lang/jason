round(0).

// some group has been formed (message from a secretary)
+!group(Kind,G) : .desire(work(_,_))  // but I am already busy
   <- .print("waiting to proceed with ",G);
      .suspend;                       // this goal will be resumed after !work (see_waiting_groups)
      !!group(Kind,G).                // try again

@lg[atomic]                           // N.B. must be atomic, otherwise st claus may handle another event +!group (from resume) before executing !!work
+!group(Kind,G)                       // I am not working, so start working with this group G
   <- !!work(Kind,G).

// I have to work with group G
+!work(Kind,G) : round(N) & N < 20  // just a stop condition
   <- .print("Working with ",Kind,"s: ",G," (",N,")");
      -+round(N+1);
      .abolish(done);
      .send(G,achieve,proceed);
      .wait(.count(done[source(_)],.length(G))); // suspends this intention until |G| dones are received
      .print("Group ",G," of ",Kind,"s has finished");
      !!see_waiting_groups. // and resume them (the resume needs to be in another intention, otherwise st claus will be working while checking new groups)
+!work(_,_) <- .print("It is better to stop for now...").

+!see_waiting_groups <-
      .resume(group(reindeer,_));    // resume reindeers first (they have priority)
      .resume(group(_,_)).
