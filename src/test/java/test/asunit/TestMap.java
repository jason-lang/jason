package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestMap {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!b(M) <- " +
            "    .map.put(M,a,10);" +
            "    .map.put(M,d,ok(3));" +
            "    .map.put(M,a,30);" +
            "    .map.put(M,c,11);" +
            "    .map.remove(M,c,11);" +
            ". \n"+

            "+!test1(S) <- " +
            "    !b(S);" +
            "    jason.asunit.print(S);" +
            ". \n"+

            "+!test2(A,V) <- " +
            "    !b(S);" +
            "    !g1(S,A,V)." +
            "+!g1(S,A,V) : .map.get(S,A,V,0) <- jason.asunit.print(tem)." +
            "+!g1(S,A,V)                     <- jason.asunit.print(naot). \n"+

            "+!test_values <- " +
            "    !b(S);" +
            "    .findall(V, .map.value(S,V), L);" +
            "    jason.asunit.print(L)."+

            "+!test_loop <- " +
            "    !b(S);" +
            "    for ( .map.key(S,K) & .map.get(S,K,V) ) {" +
            "       jason.asunit.print(K,V);"+
            "    }."+

            "+!test3 <- " +
            "    !b(S);" +
            "    .findall(K, .map.key(S,K), L);" +
            "    jason.asunit.print(L);" +
            "    .findall([K,V], .map.key(S,K) & .map.get(S,K,V), L2);" +
            "    jason.asunit.print(L2)."

        );
    }

    @Test(timeout=2000)
    public void test1() {
        ag.addGoal("test1(_)");
        ag.assertPrint("{a->30,d->ok(3)}", 20);
    }

    @Test(timeout=2000)
    public void testLoop() {
        ag.addGoal("test_loop");
        ag.assertPrint("a30", 20);
        ag.assertPrint("dok(3)", 20);
    }

    @Test(timeout=2000)
    public void test2() {
        ag.addGoal("test2(a,30)");
        ag.assertPrint("tem", 20);
        ag.clearExecutionTrace();
        ag.addGoal("test2(a,20)");
        ag.assertPrint("naot", 20);
        ag.clearExecutionTrace();
        ag.addGoal("test2(c,20)");
        ag.assertPrint("naot", 20);
        ag.clearExecutionTrace();
        ag.addGoal("test2(c,0)");
        ag.assertPrint("tem", 20);
    }

    @Test(timeout=2000)
    public void test3() {
        ag.addGoal("test3");
        ag.assertPrint("[a,d]", 20);
        ag.assertPrint("[[a,30],[d,ok(3)]]", 20);
    }

    @Test(timeout=2000)
    public void testValues() {
        ag.addGoal("test_values");
        ag.assertPrint("[30,ok(3)]", 20);
    }

}
