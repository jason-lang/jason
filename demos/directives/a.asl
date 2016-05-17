bel.

!start.
+!start
   <- .list_plans; // just list the agent's plans 
      !g.

/*
    This agent uses a declarative goal identified by 
    EBDG (Exclusive Backtracking Declarative Goal).  
    This DG tries to achieve the goal g by many 
    alternatives.
*/
      
{ begin ebdg(g) }
+!g : bel <- action1. // action1 does not achieve g
+!g       <- action2. // action2 achieves
{ end }


/*
    The above plans will be changed by the directive to:
      +!g : g.
      +!g : not (p__f(1,g)) & bel <- +p__f(1,g); action1; ?g.
      +!g : not (p__f(2,g))       <- +p__f(2,g); action2; ?g.
      -!g <- !!g.
      +g  <- .abolish(p__f(_,g)); .drop_goal(g,true).
      -g  <- .abolish(p__f(_,g)).

*/

