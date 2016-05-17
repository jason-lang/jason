// Agent bob in project MindInspection

batata(5,kg).

!start(0).

+!start(X) 
   <- .wait(1000); 
      -+vl(X); 
      .send(sample,tell,vl(X)); 
      .send(sample,untell,vl(X-1)); 
      !!start(X+1).
