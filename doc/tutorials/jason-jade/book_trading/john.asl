// Agent john in project book_trading.mas2j

/* Initial beliefs and rules */

// The book beliefs has three arguments:
//   . the book name
//   . the price
//   . the quantity in stock

book("Harry", 32, 20).
book("Jason", 75, 10).


/* Initial goals */

!registerDF.

/* Plans */

+!registerDF <- .df_register("JADE-book-trading", "book-selling"). // name, type 

/* handle CFP performatives */

// CFP
+!kqml_received(Sender, cfp, Content, MsgId)
  :  book(Content, Price, Qtd) & Qtd > 0            // if I have the book
  <- .send(Sender, propose, Price, MsgId).          // propose

+!kqml_received(Sender, cfp, Content, MsgId)
  <- .send(Sender, refuse, "not-available", MsgId). // refuse otherwise


// ACCEPT-PROPOSAL
+!kqml_received(Sender, accept_proposal, Content, MsgId)
  :  book(Content, Price, Qtd) & Qtd > 0  // If I still have the book
  <- -+book(Content, Price, Qtd-1);       // change stock
     .print("New stock for ",Content," is ", Qtd-1);
     .send(Sender, tell, Content, MsgId). // confirm

+!kqml_received(Sender, accept_proposal, Content, MsgId)
  <- .send(Sender, failure, "not-available", MsgId).
