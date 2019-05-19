// Agent sender in project tell-rule.mas2j

/* Initial beliefs and rules */

// simple rules that will be send to receiver
x(V) :- V > 10.
x(V) :- V < 30.

/* Initial goals */

!start.

/* Plans */

+!start : true
   <- .send(receiver,tellRule, [{a :- b & c}]); // send a list with a single rule
      .wait(100);
      .send(receiver,achieve,test);
      .relevant_rules(x(_),L);   // get all rules with head x(_)
      .print("Sending rules ", L);
      .send(receiver,tellRule, L).
