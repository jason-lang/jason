// Agent monitor that just print the exchanged messages

// msgSent beliefs are stored in a database

+msg_sent(Time,Id,_Irt,_Ilf,Sender,Receiver,Content)
   <- .print("Message ",Id," from ",Sender," to ",Receiver," = ", Content, " at ", Time).
