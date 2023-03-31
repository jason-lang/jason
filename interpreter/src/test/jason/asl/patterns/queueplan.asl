{ include("tester_agent.asl") }

@[test]
+!test_pattern_queue
   <- !e;
      .wait(500);
      !assert_equals( 6, .count(run(_)))
   .

// the pattern

+!e
   <- for ( .range(I,0,5)) {
         //.print(I);
         !!g(I);
      }.

+!g(I) : not .intend(g(_))
   <- +bel(I);
      .wait(50);
      !assert_equals( 1, .count(bel(_)));
      -bel(I);
      !assert_equals( 0, .count(bel(_)));
      +run(I);
      !!resume(g(_)).
+!g(I)
   <- .suspend;
      !!g(I).
+!resume(G)
   <- .resume(G).
