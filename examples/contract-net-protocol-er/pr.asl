!register.

+!register <- .df_register("participant");
              .df_subscribe("initiator").

// plan to answer a CFP
+cfp(CNPId,_Service)[source(A)]
   :   provider(A,"initiator") 
   <- .send(A,tell,refuse(CNPId)).
