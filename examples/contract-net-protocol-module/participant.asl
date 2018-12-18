// participating in CNP

// Answer a Call For Proposal
+cfp(Task)[source(A)] : acceptable(Task)
    <-  ?price(Task,Price);
        .println("Proposing ",Price," for task ",Task," from agent ",A);
        .send(A,tell, ::propose(Price));
        +participating(Task).

+cfp(Task)[source(A)] : not acceptable(Task)
    <-  .send(A,tell, ::refuse);
        .println("Refusing proposal for task ",Task," from agent ",A).

 // Possibly results of my Proposal
+accept_proposal : participating(Task)
    <-  .print("My proposal in ",this_ns," for task ",Task," won!").
        // do the task and report to initiator

+reject_proposal : participating(Task)
    <- .print("I lost CNP in ",this_ns," for task ",Task,".").
