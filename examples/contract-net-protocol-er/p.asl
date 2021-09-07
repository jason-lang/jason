// gets the price for the product,
// a random value between 100 and 110.
price(_Service,X) :- .random(R) & X = (10*R)+100.

!register.
+!register <- .df_register("participant");
              .df_subscribe("initiator").

// answer to Call For Proposal
+cfp(CNPId,Task)[source(A)]
    : provider(A,"initiator") & price(Task,Offer)
   <: false // this intention is finished only by the internal action .done, since the goal condition ('false') will never hold
   <- .send(A,tell,propose(CNPId,Offer)).
   // the plans below are relevant only while the intention for +cfp is 'running'
   {
     +accept_proposal(CNPId)
       <- .print("My proposal '",Offer,"' won CNP ",CNPId, " for ",Task,"!");
          !do;
          .send(A,tell,done(CNPId,Task));
          .print("Done!");
          .done.

     +reject_proposal(CNPId)
       <- .print("I lost CNP ",CNPId, ".");
          .done.

     +!do
       <- .print("Doing my task.....",Task);
          .wait(3000).
}
