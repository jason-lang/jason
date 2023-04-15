package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestSet {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!b(S) <- " +
            "    .set.add(S,a);" + // S is the var representing the Java Set
            "    .set.add(S,d);" +
            "    .set.add(S,w);" +
            "    .set.add(S,10);" +
            "    .set.add(S,10);" +
            "    .set.remove(S,w);" +
            "    .set.add(S,a(3));" +
            "    .set.add_all(S,[1,22,z(4),z(1)]);" +
            ". \n"+

            "+!test1(S) <- " +
            "    !b(S);" +
            "    jason.asunit.print(S);" +
            ". \n"+

            "+!test2(A) <- " +
            "    !b(S);" +
            "    !g1(S,A)." +
            "+!g1(S,A) : .member(A,S) <- jason.asunit.print(tem)." +
            "+!g1(S,A)                <- jason.asunit.print(naot). \n"+

            "+!test3 <- " +
            "    !b(S);" +
            "    .findall(K, .member(K,S), L);" +
            "    jason.asunit.print(L)." +

            "+!testToList <- " +
            "    !b(S);" +
            "    .set.to_list(S,[H|T]);" +
            "    jason.asunit.print(H,T)." +

            "+!testUn <- " +
            "    !b(S);" +
            "    .set.add_all(NS,[kk,d,a,oo]);" +
            "    .set.union(S,NS);" +
            "    .set.union(S,[8,[4,5]]);" +
            "    jason.asunit.print(S). \n"+

            "+!testInt <- " +
            "    !b(S);" +
            "    .set.add_all(NS,[kk,d,a,oo,10]);" +
            "    .set.intersection(S,NS);" +
            "    .set.intersection(S,[oo,a,d,10]);" +
            "    jason.asunit.print(S). \n" +

            "+!testDif <- " +
            "    !b(S);" +
            "    .set.add_all(NS,[kk,d,z(4),a,oo,10]);" +
            "    .set.difference(S,NS);" +
            "    jason.asunit.print(S)."
        );
    }

    @Test(timeout=2000)
    public void test1() {
        ag.addGoal("test1(_)");
        ag.assertPrint("{1,10,22,a,a(3),d,z(1),z(4)}", 20);
    }

    @Test(timeout=2000)
    public void test2() {
        ag.addGoal("test2(z(1))");
        ag.assertPrint("tem", 20);
        ag.clearExecutionTrace();
        ag.addGoal("test2(z(10))");
        ag.assertPrint("naot", 20);
    }

    @Test(timeout=2000)
    public void test3() {
        ag.addGoal("test3");
        ag.assertPrint("[1,10,22,a,a(3),d,z(1),z(4)]", 20);
    }

    @Test(timeout=2000)
    public void testToList() {
        ag.addGoal("testToList");
        ag.assertPrint("1[10,22,a,a(3),d,z(1),z(4)]", 20);
    }

    @Test(timeout=2000)
    public void testUnion() {
        ag.addGoal("testUn");
        ag.assertPrint("{1,8,10,22,[4,5],a,a(3),d,kk,oo,z(1),z(4)}", 20);
    }

    @Test(timeout=2000)
    public void testIntersection() {
        ag.addGoal("testInt");
        ag.assertPrint("{10,a,d}", 20);
    }

    @Test(timeout=2000)
    public void testDifference() {
        ag.addGoal("testDif");
        ag.assertPrint("{1,22,a(3),z(1)}", 20);
    }

}
