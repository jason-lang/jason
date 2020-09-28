{ include("tester_agent.asl") }

@[test]
+!test_pattern_singleton
   <- !e;
      .wait(500);
      !assert_equals( 1, .count(bel(_))).

// the pattern

+!e
   <- for ( .range(I,0,5)) {
         !!g(I);
      }.

+!g(I) : not .intend(g(_))
   <- .wait(100);
      +bel(I).
+!g(I).
