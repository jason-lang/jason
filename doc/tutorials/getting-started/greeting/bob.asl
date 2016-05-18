// Agent bob in project greeting.mas2j

+hello[source(A)] 
  <- .print("I receive an hello from ",A);
     .send(A,tell,hello).
