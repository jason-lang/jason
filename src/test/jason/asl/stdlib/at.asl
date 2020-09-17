/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

/**
 * time interval between the exact time it is expected that the event occur and the maximum allowed time
 * I have tried 50 ms, but on github it is causing failures quite often (maybe one in twice).
 */
time_frame(200).

@[test]
+!test_at :
    time_frame(TF)
    <-
    .nano_time(T);
    .at("now +500", {+!g(test0,T)});

    .nano_time(T0);
    .at("now +500 ms", {+!g(test1,T0)});
    .wait(500+TF);
    !assert_true(executed(test1,_,_));
    ?executed(test1,T0,T1);
    /**
     * Tolerance given by TF. Hopefully it is enough
     * for the machines that may run this test
     */
    !assert_between((T1-T0)/1000000,500,500+TF);

    .nano_time(T2);
    !assert_false(executed(g1));
    .at("now +1 s", {+!g(test2,T2)});
    .wait(1000+TF);

    !assert_true(executed(test2,_,_));
    ?executed(test2,T2,T3);
    /**
     * Tolerance given by TF. Hopefully it is enough
     * for the machines that may run this test
     */
    !assert_between((T3-T2)/1000000,1000,1000+TF);

    .at("now +1 second", {+!g(test3,_)}); // Just checking if no parse error is produced
    .at("now +1 seconds", {+!g(test4,_)}); // Just checking if no parse error is produced
    .at("now +1 m", {+!g(test5,_)}); // Just checking if no parse error is produced
    .at("now +1 minute", {+!g(test6,_)}); // Just checking if no parse error is produced
    .at("now +1 minutes", {+!g(test7,_)}); // Just checking if no parse error is produced
    .at("now +1 h", {+!g(test8,_)}); // Just checking if no parse error is produced
    .at("now +1 hour", {+!g(test9,_)}); // Just checking if no parse error is produced
    .at("now +1 hours", {+!g(test10,_)}); // Just checking if no parse error is produced
    .at("now +1 d", {+!g(test11,_)}); // Just checking if no parse error is produced
    .at("now +1 day", {+!g(test12,_)}); // Just checking if no parse error is produced
    .at("now +1 days", {+!g(test13,_)}); // Just checking if no parse error is produced
.

/**
 * Since this plan is atomic, the agent will not
 * perform !g until !test_at_atomic has an end.
 * This agent is focused in one goal, other plans
 * should not be executed, assertiong should be false.
 */
@[atomic,test]
+!test_at_atomic :
    time_frame(TF)
    <-
    .at("now +500 ms", {+!g(test100,T0)});
    .wait(500+TF);
    !assert_false(executed(test100,_,_));
.

+!g(X,T)
    <-
    .nano_time(Tend);
    +executed(X,T,Tend);
.
