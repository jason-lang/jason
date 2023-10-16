/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_send
    <-
    !create_mock_agent(rafael);

    .send(rafael, tell, vl(10));
    .send(rafael, askOne, vl(X), Y0);
    Y0 = vl(Z)[A];
    !assert_equals(10,Z);

    .send(rafael, tellHow, "+!goto(X,Y)[source(Ag)] <- .send(Ag, tell, my_position(X,Y)).");
    !wait_idle(rafael); // wait the mock to process msgs
    .send(rafael, achieve, goto(10,30));
    !wait_idle(rafael); // wait the mock to process msgs
    !assert_true(my_position(10,30));

    .send(rafael, tell, value(beer,10));
    .send(rafael, askOne, value(beer,X), Y1);
    !assert_equals(value(beer,10)[source(rafael)[source(send)]],Y1);

    .kill_agent(rafael);
.

@[test]
+!test_timeout_send
    <-
    !create_mock_agent(tom);

    .send(tom, tellHow, "+?retrieve_info(X,Y) <- .wait(100); Y = X + 1.");
    .send(tom,askOne,retrieve_info(1,Z0),retrieve_info(_,Z0),200); // give enough time to answer
    !wait_idle(tom); // wait the mock to process msgs
    !assert_equals(2,Z0);

    .send(tom,askOne,retrieve_info(10,Z1),Z1,50); // let the timeout expires
    !wait_idle(tom); // wait the mock to process msgs
    !assert_equals(timeout,Z1);

    .kill_agent(tom);
.

/**
 * Tests based on test.asunit.TestKQML
 */
@[test]
+!test_kqml_simple_send
    <-
    !create_mock_agent(maria);

    .send(maria, tell, vl(1));
    .send(maria, tell, vl(2));
    .send(maria, tell, vl(10));
    .send(maria, askOne, vl(X), Z0);
    !assert_equals(vl(10)[source(maria)[source(send)]],Z0);

    .send(maria, tellHow, "+!goto(X,Y)[source(Ag)] <- .send(Ag, tell, my_position(X,Y)).");
    !wait_idle(maria); // wait the mock to process msgs
    .send(maria, achieve, goto(10,2));
    !wait_idle(maria); // wait the mock to process msgs
    !assert_true(my_position(10,30));

    // Maria does not believes loves(X,Y)
    .send(maria, askOne, loves(X,Y), Z1);
    !assert_false(Z1);

    // Maria will believe loves(X,Y)[with given sources]
    .send(maria, tell, loves(maria,bob)[source(maria), source(mog)]);
    .send(maria, askOne, loves(X,Y), Z2);
    !assert_equals(loves(maria,bob)[source(maria)[source(send)[source(maria),source(mog)]]],Z2);

    .send(maria, askOne, vl(_), vl(X0));
    .send(maria, askOne, vl(_));
    !assert_equals(10,X0);

    .send(maria, tellHow, "+?t2(X)[source(A)] : vl(Y) <- X = 10 + Y.");
    .send(maria, askOne, t2(_), X1);
    !assert_equals(t2(20)[source(maria)],X1);

    .send(maria, askOne, t1(_), X2);
    !assert_false(X2);

    .send(maria, tellHow,
        {+!gg <- .add_plan(
          {+!kqml_received(Sender, askOne, fullname, ReplyWith)  <- .send(Sender,tell,name("Maria dos Santos"), ReplyWith)},
          kk,
          begin)
        }
    );
    !wait_idle(maria); // wait the mock to process msgs
    .send(maria, achieve, gg);
    .send(maria, askOne, fullname, X3);
    !assert_equals(name("Maria dos Santos")[source(maria)],X3);

    .send(maria, tell, myv(10));
    .send(maria, askOne, myv(_));

    .send(maria, tell, myv(20));
    .send(maria, askOne, myv(_), X5);
    !assert_equals(myv(20)[source(maria)[source(send)]],X5);

    .send(maria, askAll, vl(_), L0);
    !assert_equals([
        vl(10)[source(maria)[source(send)]],
        vl(2)[source(maria)[source(send)]],
        vl(1)[source(maria)[source(send)]]
    ],L0);

    .send(maria, askAll, t1(_), L1);
    !assert_equals([],L1);

    //telling how to perform plan +!hello
    .plan_label(Plan,hp);
    .send(maria,tellHow,Plan);
    .send(maria, tellHow, "-!hello(Who) <- +failed(Who).");
    .send(maria,achieve,hello(bob));
    !wait_idle(maria); // wait the mock to process msgs
    .send(maria,askOne,greeted(bob), W0);
    !assert_equals(greeted(bob)[source(maria)],W0);
    .send(maria,untell,greeted(bob));

    // UN-telling how to perform plan +!hello
    .send(maria,untellHow,hp);
    !wait_idle(maria); // wait the mock to process msgs
    .send(maria,achieve,hello(tom));
    //expected that -!hello was performed
    .send(maria, askOne, greeted(tom), W1);
    !assert_false(W1);
    .send(maria, askOne, failed(tom), W2);
    !assert_equals(failed(tom)[source(maria)],W2);

    .send(maria,askHow,{+!goto(_,_)},[{@L +!T : C <- B}]);
    !assert_true(T = goto(_,_)[source(_)]);
    !assert_true(C);
    //The plan body starts with ".send("
    !assert_true(.substring(".send(",B,0));
    .kill_agent(maria);
.

@hp +!hello(Who)  <- +greeted(Who).
