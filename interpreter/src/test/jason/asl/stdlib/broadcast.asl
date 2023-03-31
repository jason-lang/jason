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
    .log(warning,"TODO: sometimes !assert_equals(jomi,A1) is not being performed properly.");
/*
At least in my (Cleber's) machine the next assert is often failing. Let us say, it fails 1 on every 5 attempts.
The random problem seems to be happening on rule intention_test_goal defined and used on test_assert.asl. I have tried to
depict that into three definitions but the problem persisted.

intention_test_goal(Goal,Test,Label,Line,Src) :- .intention(ID,_,[ im(Label,TGoal,{ Test; _ },_)|_],current) &
    TGoal = {+!Goal[_|_]} & _[code_line(Line),code_src(Src)] = Label.
intention_test_goal(Goal,Test,Label,Line,Src) :- .intention(ID,_,[ im(Label,TGoal,{ Test; _ },_)|_],current) &
    TGoal = {-!Goal[_|_]} & _[code_line(Line),code_src(Src)] = Label.
intention_test_goal(Goal,Test,Label,Line,Src) :- .intention(ID,_,[ im(Label,TGoal,{ Test; _ },_)|_],current).

The message of the problem says "[broadcast] assert_equals expecting 'jomi' (atom) and having 'jomi' (atom) could not be performed! FAILED!"

Changing the third definiotn to intention_test_goal(Goal,Test,Label,Line,Src) :- .intention(ID,_,[ im(Label,TGoal,{ Test; _ },_)|_],_). make it works
without errors but the "_" now can be any state of intention. So, a possible explaination is that sometimes .intention
is not retrieving the current intention properly.
*/
    //!assert_equals(jomi,A1);
    !assert_equals(broadcast,B1);

    .send(olivier, askOne, season(X), Y2);
    Y2 = season(Z2)[source(A2)[source(B2)]];
    !assert_equals(summer,Z2);
    !assert_equals(olivier,A2);
    !assert_equals(broadcast,B2);

    .kill_agent(jomi);
    .kill_agent(olivier);
.
