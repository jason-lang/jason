package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestKQML {

    TestAgent bob, maria;

    // initialisation of the agents
    @Before
    public void setupAgs() {
        bob   = new TestAgent("bob");
        maria = new TestAgent("maria");

        // defines the agent's AgentSpeak code
        bob.parseAScode(
            "+!simple_send <- .send(maria, tell, vl(10)); "+
            "                 .send(maria, achieve, goto(10,2)); " +
            "                 .send(maria, tell, loves(maria,bob)[source(maria), source(mog)]). " +

            "+!send_ask1   <- .send(maria, askOne, vl(_), vl(X)); " +
            "                 .send(maria, askOne, vl(_)); " +
            "                 act1(X). "+
            "+!send_ask2   <- .send(maria, askOne, t2(_), A); "+
            "                 jason.asunit.print(A). " +
            "+!send_ask3   <- .send(maria, askOne, t1(_), A); "+
            "                 jason.asunit.print(t1,\" \",A). "+
            "+!send_ask4   <- .send(maria, askOne, fullname, A); "+
            "                 jason.asunit.print(A). "+
            
            "+!send_ask5   <- .send(maria, tell, myv(10)); .send(maria, askOne, myv(_)). "+
            "+!send_ask6   <- .send(maria, tell, myv(10)); .send(maria, askOne, myv(_), A); jason.asunit.print(A). "+

            "+!send_askAll1 <- .send(maria, askAll, vl(_), L); "+
            "                  jason.asunit.print(L). "+
            "+!send_askAll2 <- .send(maria, askAll, t1(_), L); "+
            "                  jason.asunit.print(L). "+

            "+!send_tellHow   <- .plan_label(Plan,hp);"+
            "                    .send(maria,tellHow,Plan); "+
            "                    .send(maria,achieve, hello(bob)). "+
            "+!send_untellHow <- .send(maria,untellHow,hp). "+

            "@hp +!hello(Who)  <- jason.asunit.print(\"Hello \",Who)."
        );

        maria.parseAScode(
            "vl(1). vl(2). " +
            "+!goto(X,Y)[source(Ag)] <- act(X,Y,Ag). "+
            "+?t2(X)[source(A)] : vl(Y) <- X = 10 + Y; jason.asunit.print(A)."+
            "+!kqml_received(Sender, askOne, fullname, ReplyWith)  <- .send(Sender,tell,\"Maria dos Santos\", ReplyWith). "
        );

    }

    @Test(timeout=2000)
    public void testSend() {
        bob.addGoal("simple_send");
        bob.assertIdle(5);                         // bob sent the messages
        maria.assertBel("vl(10)[source(bob)]", 5); // maria received tell
        maria.assertAct("act(10,2,bob)", 5);       // maria received achieved
        maria.assertBel("loves(maria,bob)[source(bob)[source(maria),source(mog)]]", 5);
    }

    @Test(timeout=2000)
    public void testAsk() {
        bob.addGoal("send_ask1");
        bob.addGoal("send_ask2");
        bob.assertIdle(10);   // let bob to send the messages
        maria.assertIdle(10); // let maria to process the messages
        bob.assertAct("act1(1)", 15);
        maria.assertIdle(5);
        bob.assertBel("vl(1)[source(maria)]", 5); // answer of maria for second askOne (assynchronous)
        maria.assertPrint("bob", 5);
        bob.assertPrint("t2(11)", 5); // answer for ask2
    }

    @Test(timeout=2000)
    public void testAsk3() {
        bob.addGoal("send_ask3");
        bob.assertIdle(10);   // let bob to send the messages
        maria.assertIdle(10); // let maria to process the messages
        bob.assertPrint("t1 false", 5); // answer for ask3

        bob.addGoal("send_ask4");
        bob.assertIdle(10);
        maria.assertIdle(10);
        bob.assertPrint("Maria dos Santos", 5); // answer for ask3
    }

    @Test(timeout=2000)
    public void testAsk5() {
        bob.addGoal("send_ask5");
        bob.assertIdle(20);   // let bob to send the messages
        maria.assertIdle(20); // let maria to process the messages
        bob.assertBel("myv(10)[source(maria)[source(bob)]]", 10);
    }

    @Test(timeout=2000)
    public void testAsk6() {
        bob.addGoal("send_ask6");
        bob.assertIdle(20);   // let bob to send the messages
        maria.assertIdle(20); // let maria to process the messages
        bob.assertPrint("myv(10)[source(maria)[source(bob)]]", 20);
    }

    @Test(timeout=2000)
    public void testAskAll() {
        bob.addGoal("simple_send");
        bob.addGoal("send_askAll1");
        bob.assertIdle(10);
        maria.assertIdle(15);
        bob.assertPrint("[vl(10)[source(maria)[source(bob)]],vl(1)[source(maria)],vl(2)[source(maria)]]", 5);

        bob.addGoal("send_askAll2");
        bob.assertIdle(10);
        maria.assertIdle(10);
        bob.assertPrint("[]", 5);
    }

    @Test(timeout=2000)
    public void testTellHow() {
        bob.addGoal("send_tellHow");
        bob.assertIdle(10);
        maria.assertPrint("Hello bob", 20);

        bob.addGoal("send_untellHow");
        bob.assertIdle(10);
        maria.assertIdle(10);
        Assert.assertTrue(maria.getPL().get("hp") == null);
    }
}
