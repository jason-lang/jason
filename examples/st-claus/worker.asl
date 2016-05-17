msg("Ho, ho, ho!  Let's meet in the study!") :- kind(elf).
msg("Ho, ho, ho!  Let's deliver toys!")      :- kind(reindeer).

!start. // initial goal, notify my secretary that I am ready
+!start : secretary(S) <- .send(S,achieve,iamhere).

// do the job asked by St Claus
+!proceed[source(A)] : msg(M)      
   <- .print(M); 
      .send(A,tell,done); 
      .wait( math.random(20)+10 );
      !!start.
