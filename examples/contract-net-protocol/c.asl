/* Initial beliefs and rules */

all_proposals_received(CNPId,NP) :-              // NP = number of participants
     .count(propose(CNPId,_)[source(_)], NO) &   // number of proposes received
     .count(refuse(CNPId)[source(_)], NR) &      // number of refusals received
     NP = NO + NR.


/* Initial goals */

!cnp(1,fix(computer)).
!cnp(2,banana).

!register.
+!register <- .df_register(initiator).

/* Plans */

// start the CNP
+!cnp(Id,Task)
   <- !call(Id,Task,LP);
      !bid(Id,LP);
      !winner(Id,LO,WAg);
      !result(Id,LO,WAg).
+!call(Id,Task,LP)
   <- .print("Waiting participants for task ",Task,"...");
      .wait(2000);  // wait participants introduction
      .df_search("participant",LP);
      .print("Sending CFP to ",LP);
      .send(LP,tell,cfp(Id,Task)).
+!bid(Id,LP) // the deadline of the CNP is now + 4 seconds (or all proposals received)
   <- .wait(all_proposals_received(Id,.length(LP)), 4000, _).
+!winner(Id,LO,WAg)
   :  .findall(offer(O,A),propose(Id,O)[source(A)],LO) & LO \== [] // there is a offer
   <- .print("Offers are ",LO);
      .min(LO,offer(WOf,WAg)); // the first offer is the best
      .print("Winner is ",WAg," with ",WOf).
+!winner(_,_,nowinner). // no offer case

+!result(_,[],_).
+!result(CNPId,[offer(_,WAg)|T],WAg) // announce result to the winner
   <- .send(WAg,tell,accept_proposal(CNPId));
      !result(CNPId,T,WAg).
+!result(CNPId,[offer(_,LAg)|T],WAg) // announce to others
   <- .send(LAg,tell,reject_proposal(CNPId));
      !result(CNPId,T,WAg).
