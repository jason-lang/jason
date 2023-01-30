// alternative implementation without .wait(<condition>), see plan for !bid

/* Initial beliefs and rules */

all_proposals_received(CNPId, NP)                // NP: number of participants
  :- .count(propose(CNPId,_)[source(_)], NO) &   // NO: number of proposes received
     .count(refuse(CNPId)[source(_)], NR) &      // NR: number of refusals received
     NP = NO + NR.

/* Initial goals */

!run.
!register.
+!register <- .df_register(initiator).

/* Plans */
+!run
   <- .wait(1000);  // wait participants introduction
      !!cnp(1,fix(computer));
      !!cnp(2,banana);
   .

+!cnp(Id,Task) <- !call(LP); !bids(LP); !winner(LO,W); !result(LO,W). {
    // the plans below are !cnp sub-plans and
    // are relevant only while !cnp is being pursued

    +!call(LP)
       <- .df_search("participant",LP);
          .print("Sending CFP to ",LP);
          .send(LP,tell,cfp(Id,Task)).

    +!bids(LP) : all_proposals_received(Id, .length(LP)). // all proposals received already! bids is thus achieved
    +!bids(LP) : NP = .length(LP) // wait either for all proposals/refuses or a timeout
       <: false // this intention is finished only by the internal action .done, since the goal condition ('false') will never hold
       <- .wait(4000); .done. // wait for 4 seconds and then finish the intention
       {  // the two plans below are relevant only while !bids is being pursued
          +propose(Id,_) : all_proposals_received(Id, NP) <- .done. // if a propose is received, test if all are, if so, finish the intention
          +refuse(Id)    : all_proposals_received(Id, NP) <- .done.
       }

    +!winner(LO,WAg)
        : .findall(offer(O,A),propose(Id,O)[source(A)],LO) & LO \== []
       <- .print("Offers are for ",Task," are ",LO);
          .min(LO,offer(WOf,WAg)); // sort offers, the first is the best
          .print("Winner for ",Task," is ",WAg," with ",WOf).
    +!winner([],nowinner)
       <- .print("CNP ",Id," with no offer!").

    +!result(LO,WAg)
       <- for( .member( offer(_,WAg), LO) ) {
             .send(WAg,tell,accept_proposal(Id));
          }
          for( .member( offer(_,Ag), LO) & Ag \== WAg) {
             .send(Ag,tell,reject_proposal(Id));
          }.
}
