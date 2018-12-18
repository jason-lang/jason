/* Initial beliefs and rules */

all_proposals_received(CNPId)
  :- .count(introduction(participant,_),NP) &    // number of participants
     .count(propose(CNPId,_)[source(_)], NO) &   // number of proposes received
     .count(refuse(CNPId)[source(_)], NR) &      // number of refusals received
     NP = NO + NR.

/* Initial goals */

!startCNP(1,fix(computer)).
//!startCNP(2,banana).

/* Plans */

// start the CNP
+!startCNP(Id,Task)
   <- .print("Waiting participants for task ",Task,"...");
      .wait(2000);  // wait participants introduction
      +cnp_state(Id,propose);   // remember the state of the CNP
      .findall(Name,introduction(participant,Name),LP);
      .print("Sending CFP to ",LP);
      .send(LP,tell,cfp(Id,Task));
      // the deadline of the CNP is now + 4 seconds (or all proposals were received)
      .wait(all_proposals_received(Id), 4000, _);
      !contract(Id).

// this plan needs to be atomic so as not to accept
// proposals or refusals while contracting
@lc1[atomic]
+!contract(CNPId)
   :  cnp_state(CNPId,propose)
   <- -cnp_state(CNPId,_);
      +cnp_state(CNPId,contract);
      .findall(offer(O,A),propose(CNPId,O)[source(A)],L);
      .print("Offers are ",L);
      L \== []; // constraint the plan execution to at least one offer
      .min(L,offer(WOf,WAg)); // sort offers, the first is the best
      .print("Winner is ",WAg," with ",WOf);
      !announce_result(CNPId,L,WAg);
      -+cnp_state(CNPId,finished).

// nothing todo, the current phase is not 'propose'
@lc2 +!contract(_).

-!contract(CNPId)
   <- .print("CNP ",CNPId," has failed!").

+!announce_result(_,[],_).
// announce to the winner
+!announce_result(CNPId,[offer(_,WAg)|T],WAg)
   <- .send(WAg,tell,accept_proposal(CNPId));
      !announce_result(CNPId,T,WAg).
// announce to others
+!announce_result(CNPId,[offer(_,LAg)|T],WAg)
   <- .send(LAg,tell,reject_proposal(CNPId));
      !announce_result(CNPId,T,WAg).
