/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_broadcast
    <-
    !create_mock_agent(jomi);

    .send(jomi, askOne, value(X00), Y00);
    !assert_false(Y00);

    .broadcast(tell,value(10));

    // This agent should not receive its own message
    !assert_false(value(10));

    // Ask to the other existing agent, jomi
    .send(jomi, askOne, value(X), Y0);
    Y0 = value(Z0)[A0];
    !assert_equals(10,Z0);

    !create_mock_agent(olivier);

    .send(olivier, askOne, value(Z00));
    !assert_false(value(10));

    .broadcast(tell,season(summer));

    .send(jomi, askOne, season(X), Y1);
    Y1 = season(Z1)[source(A1)[source(B1)]];
    !assert_equals(summer,Z1);
    !assert_equals(jomi,A1);
    !assert_equals(broadcast,B1);

    .send(olivier, askOne, season(X), Y2);
    Y2 = season(Z2)[source(A2)[source(B2)]];
    !assert_equals(summer,Z2);
    !assert_equals(olivier,A2);
    !assert_equals(broadcast,B2);

    .kill_agent(jomi);
    .kill_agent(olivier);
.
