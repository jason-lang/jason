/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

!execute_test_plans.

/**
 * Tests based on from http://jason.sourceforge.net/api/jason/stdlib/send.html
 */
@[test]
+!test_launch_send
    <-
    .create_agent(rafael);
    .send(rafael, tell, vl(10));
    .send(rafael, askOne, vl(X), Y0);
    !test_rafael_vl_10(Y0);

    .send(rafael, tellHow, "+!goto(X,Y)[source(Ag)] <- .send(Ag, tell, my_position(X,Y)).");
    .send(rafael, achieve, goto(10,30));
    .wait(200);
    !test_rafael_reply_goto;

    .send(rafael, tell, value(beer,10));
    .send(rafael, askOne, value(beer,X), Y1);
    !test_rafael_value_10(Y1);

    .send(rafael,askOne,value(beer,X),Y2,2000);
    !test_rafael_value_10(Y2);

    .log(warning,"TODO: .send(tom,askOne,value(beer,X),Y3,500) is blocked, plan below (test_tom_value_600) is not being executed!")
    //.send(tom,askOne,value(beer,X),Y3,500);
    .wait(550); // let the timeout expires
    //!test_tom_value_600(Y3);
.

+!test_rafael_vl_10(Y)
    <-
    Y = vl(Z)[A];
    !assert_equals(10,Z);
.

+!test_rafael_reply_goto
    <-
    !assert_true(my_position(10,30));
.

+!test_rafael_value_10(Y)
    <-
    Y = value(beer,Z)[A]
    !assert_equals(10,Z);
.

+!test_tom_value_600(Y)
    <-
    Y = value(beer,Z)[A];
    !assert_equals(600,Z);
.

/**
 * Tests based on test.asunit.TestKQML
 */
@[test]
+!test_kqml_simple_send
    <-
    .create_agent(maria);
    .send(maria, tell, vl(1));
    .send(maria, tell, vl(2));
    .send(maria, tellHow, "+!goto(X,Y)[source(Ag)] <- .send(Ag, tell, my_position(X,Y)).");
    .send(maria, tellHow, "+?t2(X)[source(A)] : vl(Y) <- X = 10 + Y; .print(A).");
    .send(maria, tellHow,
        "+!kqml_received(Sender, askOne, fullname, ReplyWith)  <- .send(Sender,tell,\"Maria dos Santos\", ReplyWith)."
    );

    !simple_send;

    .send(maria, askOne, vl(X), Z0);
    !test_simple_send_vl(Z0);

    !test_maria_reply_goto;

    .send(maria, askOne, loves(X,Y), Z1);
    !test_simple_send_loves(Z1);
.

+!simple_send <- .send(maria, tell, vl(10));
                 .send(maria, achieve, goto(10,2));
                 .send(maria, tell, loves(maria,bob)[source(maria), source(mog)]);
                 .log(warning,"TODO: Shouldn't this source 'mog' appear on test_simple_send_loves?");
.

+!test_maria_reply_goto
    <-
    !assert_true(my_position(10,30));
.

+!test_simple_send_vl(Z)
    <-
    Z = vl(X)[A]
    A = source(S)[AA];
    AA = source(SS);
    !assert_equals(10,X);
    !assert_equals(maria,S);
    !assert_equals(send,SS);
.

+!test_simple_send_loves(Z)
    <-
    Z = loves(X,Y)[A];
    A = source(S)[AA];
    AA = source(SS);
    !assert_equals(maria,X);
    !assert_equals(bob,Y);
    !assert_equals(maria,S);
    !assert_equals(send,SS);
.

/**
 * Tests based on test.asunit.TestKQML
 */
 @[test]
 +!test_kqml_send_ask
     <-
     .log(warning,"TODO: Tests on .send still under development");
     !send_ask1;
     !send_ask2;
     !send_ask3;
     !send_ask4;
     !send_ask5;
     !send_ask6;
     !send_askAll1;
     !send_askAll2;
     !send_tellHow;
     !send_untellHow;
     !send_askHow;
.

+!send_ask1   <- .send(maria, askOne, vl(_), vl(X));
                 .send(maria, askOne, vl(_));
                 .print(X).

+!send_ask2   <- .send(maria, askOne, t2(_), A);
                 .print(A).

+!send_ask3   <- .send(maria, askOne, t1(_), A);
                 .print(t1," ",A).

+!send_ask4   <- .send(maria, askOne, fullname, A);
                 .print(A).

+!send_ask5   <- .send(maria, tell, myv(10)); .send(maria, askOne, myv(_)).

+!send_ask6   <- .send(maria, tell, myv(10)); .send(maria, askOne, myv(_), A); .print(A).

+!send_askAll1 <- .send(maria, askAll, vl(_), L);
                  .print(L).

+!send_askAll2 <- .send(maria, askAll, t1(_), L);
                  .print(L).

+!send_tellHow   <- .plan_label(Plan,hp);
                    .send(maria,tellHow,Plan);
                    .send(maria,achieve, hello(bob)).

+!send_untellHow <- .send(maria,untellHow,hp).

+!send_askHow    <- .send(maria,askHow,{+!goto(_,_)},LP); .add_plan(LP); .print(LP).


@hp +!hello(Who)  <- .print("Hello ",Who).
