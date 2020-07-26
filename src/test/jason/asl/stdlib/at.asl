/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

!execute_test_plans.

@[test]
+!test_launch_at
    <-
    .nano_time(T0);
    !assert_false(executed(g0));
    .log(warning,"TODO: Error on at using miliseconds! .at with no metric unit is supposed to be in mili!");
    //.at("now +500", {+!g(test1,T0)});
    .wait(550);
    //!test_at_test1;

    .nano_time(T2);
    !assert_false(executed(g1));
    .at("now +1 s", {+!g(test2,T2)});
    .wait(1050);
    .log(warning,"TODO: at not working as expected! Is the thread of the agent stopped? How to test at?");
    //!test_at_test2;

    .log(warning,"TODO: Create tests for different formats of 'when' parameter");
    //.at("now +1 second", {+!g(g1)});
    //.at("now +1 seconds", {+!g(g1)});
.

+!test_at_test1
    <-
    !assert_true(executed(test1,_,_));

    ?executed(test1,T0,T1);
    Tmili = (T1-T0)/1000000;
    !assert_greaterthan(Tmili,500);
.

+!test_at_test2
    <-
    !assert_true(executed(test2,_,_));

    ?executed(test2,T0,T1);
    Tmili = (T1-T0)/1000000;
    !assert_greaterthan(Tmili,1000);
.

+!g(X,T)
    <-
    .nano_time(Tend);
    +executed(X,T,Tend);
.
