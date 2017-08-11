// Agent receiver in project tell-rule.mas2j

/* Initial beliefs */

b.
c.

/* Plans */

+!test : a <- .print(ok).
+!test     <- .print(not_ok).

// customisation of KQML performative tellRule
+!kqml_received(A,tellRule,Rules,_)
   <- .print("Received rule(s) ",Rules, " from ",A);
      for ( .member(R, Rules) ) {
        +R[source(A)];
      }
      // get all rules and print them
      .relevant_rules(_,LR);
      .print("Rules: ",LR).

