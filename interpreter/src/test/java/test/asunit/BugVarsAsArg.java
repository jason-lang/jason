package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by Stephen Cranefield  -- see jason list */
public class BugVarsAsArg {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "test_rule(A,a(A)). "+
            "ml0(L,L). "+
            "ml(V1,V2,R) :- ml0([V1,V2],R). "+

            "append([], L, L). "+
            "append([H|T], L1, [H|L2]) :- append(T, L1, L2). "+

            "+!pml0(L,L). "+
            "+!pml(V1,V2,R) <- !pml0([V1,V2],R). "+

            "+!g(V1,V2) <- V2=p(V1,H). "+

            "+!test1  <- ?test_rule(T,A); A = a(V); T=45; jason.asunit.print(V). "+
            "+!test2  <- ?ml(A,B,L); A=1; B=2; jason.asunit.print(L). "+
            "+!test2p <- !pml(A,B,L); A=1; B=2; jason.asunit.print(L). "+
            "+!test3  <- L=[X,Y]; ?append(L, [Z], L2); Z=a; X=f; Y=i; jason.asunit.print(L2). " +
            "+!test4  <- X=3; !g(X,R); H=6; jason.asunit.print(R)." // the H from !g should not mix the G here
        );
    }


    @Test(timeout=2000)
    public void testRule1() {
        ag.addGoal("test1");
        ag.assertPrint("45", 5);
    }

    @Test(timeout=2000)
    public void testRule2() {
        ag.addGoal("test2");
        ag.assertPrint("[1,2]", 5);
    }

    @Test(timeout=2000)
    public void testRule2p() {
        ag.addGoal("test2p");
        ag.assertPrint("[1,2]", 15);
    }

    @Test(timeout=2000)
    public void testRule3() {
        ag.addGoal("test3");
        ag.assertPrint("[f,i,a]", 10);
    }
    @Test(timeout=2000)
    public void testRule4() {
        ag.addGoal("test4");
        ag.assertPrint("p(3,_", 10);
    }
}
