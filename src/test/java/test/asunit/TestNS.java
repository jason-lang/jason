package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestNS {

    TestAgent bob;

    // initialisation of the agents
    @Before
    public void setupAgs() {
        bob   = new TestAgent("bob");

        // defines the agent's AgentSpeak code
        bob.parseAScode(
            "+!bb <- "+
            "   +b(0, k); "+
            "   +ns1::b(10, ns1::test); "+
            "   +ns2::b(20, a); "+
            "   ?b(X,Y); "+
            "   jason.asunit.print(X,Y); "+
            "   ?ns1::b(X2,ns1::_); "+
            "   jason.asunit.print(X2); "+
            "   ?ns2::b(X3,_); "+
            "   jason.asunit.print(X3); "+
            "   +Y::b(40,o); "+
            "   +default::b(50,o); "+
            ". "+
            "+!fa <- "+
            "   .findall(FV, b(FV,_), L); "+
            "   jason.asunit.print(L); "+
            ". " +
            "+!fa(NS) <- "+
            "   .findall(FV, NS::b(FV,_::_), L); "+
            "   jason.asunit.print(L); "+
            ". \n" +
            "+!ia : .namespace(ns1) <- "+
            "   .findall(J, .namespace(J), L); "+
            "   jason.asunit.print(L); "+
            "   .findall(X, .namespace(NS) & NS::b(X,_::_), L2); "+
            "    jason.asunit.print(L2); "+
            "   .findall(NS, NS::b(_,o), L3); "+ // all name spaces with b(_,o)
            "    jason.asunit.print(L3); "+
            ". " +
            "+!var  <- "+
            "   .findall(X, X, L); "+
            "   jason.asunit.print(L); "+
            ". " +
            "+!builder  <- "+
            "   X =.. [p,[10,20],[a1]]; "+
            "   jason.asunit.print(X); "+
            "   nn::p(10,20)[a1] =.. V; "+
            "   jason.asunit.print(V); "+
            "   N::Y =.. [ns,p,[10,20],[a1]]; "+
            "   jason.asunit.print(N,\"::\",Y); "+
            "   jason.asunit.print(N::Y). "+
            "{ begin namespace(k2,local) }" +
            "    b(550, ink2). "+
            "{ end }"
        );

    }

    @Test(timeout=2000)
    public void testBB() {
        bob.addGoal("bb");
        bob.assertPrint("0k", 10);
        bob.assertPrint("10", 10);
        bob.assertPrint("20", 10);
        bob.assertBel("k::b(40,o)", 10);
        bob.assertBel("b(50,o)", 10);

        bob.addGoal("fa");
        bob.assertPrint("[50,0]", 10);
        bob.addGoal("fa(default)");
        bob.assertPrint("[50,0]", 10);
        bob.addGoal("fa(ns1)");
        bob.assertPrint("[10]", 10);

        bob.addGoal("ia");
        bob.assertPrint("[default,ns1,ns2,k]", 10);
        bob.assertPrint("[50,0,10,20,40]", 10);
        bob.assertPrint("[default,k]", 10);

        bob.addGoal("var");
        bob.assertPrint("[b(50,o)[source(self)],b(0,k)[source(self)]]", 10);
    }

    @Test(timeout=2000)
    public void testOperator() {
        bob.addGoal("builder");
        bob.assertPrint("p(10,20)[a1]", 10);
        bob.assertPrint("[nn,p,[10,20],[a1]]", 10);
        bob.assertPrint("ns::p(10,20)[a1]", 10);
        bob.assertPrint("ns::p(10,20)[a1]", 10);
    }

}
