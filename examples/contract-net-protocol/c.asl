/* Initial beliefs and rules */

all_proposals_received(CNPId,NP) :-              // NP = number of participants
     .count(propose(CNPId,_)[source(_)], NO) &   // number of proposes received
     .count(refuse(CNPId)[source(_)], NR) &      // number of refusals received
     NP = NO + NR.


/* Initial goals */

!startCNP(1,fix(computer)).
//!startCNP(2,banana).

!register.
+!register <- .df_register(initiator).

/* Plans */

// start the CNP
+!startCNP(Id,Task)
   <- .print("Waiting participants for task ",Task,"...");
      .wait(2000);  // wait participants introduction
      .df_search("participant",LP);
      .print("Sending CFP to ",LP);
      .send(LP,tell,cfp(Id,Task));
      // the deadline of the CNP is now + 4 seconds (or all proposals were received)
      .wait(all_proposals_received(Id,.length(LP)), 4000, _);
      !contract(Id).

+!contract(CNPId)
   :  .findall(offer(O,A),propose(CNPId,O)[source(A)],L) & L \== [] // there is a offer
   <- .print("Offers are ",L);
      .min(L,offer(WOf,WAg)); // the first offer is the best
      .print("Winner is ",WAg," with ",WOf);
      !announce_result(CNPId,L,WAg).
+!contract(_). // no offer case

+!announce_result(_,[],_).
// announce to the winner
+!announce_result(CNPId,[offer(_,WAg)|T],WAg)
   <- .send(WAg,tell,accept_proposal(CNPId));
      !announce_result(CNPId,T,WAg).
// announce to others
+!announce_result(CNPId,[offer(_,LAg)|T],WAg)
   <- .send(LAg,tell,reject_proposal(CNPId));
      !announce_result(CNPId,T,WAg).
