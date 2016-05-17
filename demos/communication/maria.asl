// Agent maria in project Communication.mas2j

vl(1).
vl(2).

//!hello(maria).

/* The plan below is triggered when a tell message is 
   received. It is like the belief addition, but with 
   a source that is not self.
*/
+vl(X)[source(Ag)] 
   :  Ag \== self
   <- .print("Received tell ",vl(X)," from ", Ag).
   
/* Plan triggered when an achieve message is received.
   It is like a new goal, but with a different source.
*/
+!goto(X,Y)[source(Ag)] : true
   <- .println("Received achieve ",goto(X,Y)," from ", Ag).

/* When Bob asked me about t2(X), I do not have it in my 
   belief base. So the event "+?t2(X)" is created and
   handled by the plan below.
*/
+?t2(X) : vl(Y) <- X = 10 + Y.

/* The plan below is used to customise the askOne performative.
   askOne (and askAll) normally just consult the agent's belief base.
   with this customisation, the answer can be something
   not in belief base (e.g., the result of some operations) and
   not handled by +? events. This customisation is applied only 
   when the content of the askOne message is "fullname".
   Note that in order to get the full name the best solution it to use
   +? events, the fullname is used here just to illustrate the 
   kqmlReceived customisation.
*/
+!kqml_received(Sender, askOne, fullname, ReplyWith) : true
   <- .send(Sender,tell,"Maria dos Santos", ReplyWith). // send the answer

