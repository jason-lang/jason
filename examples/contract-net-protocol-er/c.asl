/* Initial beliefs and rules */

all_proposals_received(CNPId)
  :- .count(introduction(participant,_),NP) &    // number of participants
     .count(propose(CNPId,_)[source(_)], NO) &   // number of proposes received
     .count(refuse(CNPId)[source(_)], NR) &      // number of refusals received
     NP = NO + NR.

/* Initial goals */

!cnp(1,fix(computer)).
!cnp(2,banana).

/* Plans */

+!cnp(Id,Task) {
    <- !start; !bids(O); !contract(O,W); !announce(O,W).

    +!start
       <- .print("Waiting participants for task ",Task,"...");
          .wait(2000);  // wait participants introduction
          +cnp_state(Id,propose);   // remember the state of the CNP
          .findall(Name,introduction(participant,Name),LP);
          .print("Sending CFP to ",LP);
          .send(LP,tell,cfp(Id,Task)).

    +!bids(L)
       <- // the deadline of the CNP is now + 4 seconds (or all proposals were received)
          .wait(all_proposals_received(Id), 4000, _);
          .findall(offer(O,A),propose(Id,O)[source(A)],L);
          .print("Offers are for ",Task," are ",L).

    +!contract([],nowinner)
       <- .print("CNP ",Id," with no offers!").

    // this plan needs to be atomic so as not to accept
    // proposals or refusals while contracting
    @lc1[atomic]
    +!contract(O,WAg)
       :  cnp_state(Id,propose)
       <- -cnp_state(Id,_);
          +cnp_state(Id,contract);
          .min(O,offer(WOf,WAg)); // sort offers, the first is the best
          .print("Winner for ",Task," is ",WAg," with ",WOf).
    // nothing todo, the current phase is not 'propose'
    +!contract(_,_).

    +!announce([],_)
       <- -+cnp_state(Id,finished).
    // announce to the winner
    +!announce([offer(_,WAg)|T],WAg)
       <- .send(WAg,tell,accept_proposal(Id));
          !announce(T,WAg).
    // announce to others
    +!announce([offer(_,LAg)|T],WAg)
       <- .send(LAg,tell,reject_proposal(Id));
          !announce(T,WAg).
}
