// gets the price for the product,
// a random value between 100 and 110.
price(_Service,X) :- .random(R) & X = (10*R)+100.

plays(initiator,c).

/* Plans */

// send a message to the initiator introducing myself as a participant
+plays(initiator,In)
   :  .my_name(Me)
   <- .send(In,tell,introduction(participant,Me)).

// answer to Call For Proposal
+cfp(CNPId,Task)[source(A)]
    : plays(initiator,A) & price(Task,Offer)
   <: false {

   <- .send(A,tell,propose(CNPId,Offer)).

   +accept_proposal(CNPId)
     <- .print("My proposal '",Offer,"' won CNP ",CNPId, " for ",Task,"!").
        // do the task and report to initiator

   +reject_proposal(CNPId)
     <- .print("I lost CNP ",CNPId, ".");
        //.drop_intention
        .
}
