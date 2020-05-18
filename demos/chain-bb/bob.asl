// Agent bob in project chainBB.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true
  <- +boss(1,"Vercouter");
     +boss(2,"Rosine");
     +boss(2,"Boissier"); // will replace "boss(2,Rosine)" since both have the same key
     +boss(math.round(math.random(10)), "Picard");
     ?boss(2,X); .print("Boss 2 is (should be Boissier or Picard): ",X).
