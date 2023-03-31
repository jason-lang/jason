package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Test;

public class TestCopyTerm {

    @Test(timeout=2000)
    public void testUnnamedVar() {
        TestAgent ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "copy_term(T,T). "+
            "+!test1 : copy_term(a(B),Y) <- jason.asunit.print(Y). "+
            "+!test2 : copy_term(a(B),Y) <- B=10; jason.asunit.print(Y). "
        );
        ag.addGoal("test1");
        ag.assertPrint("a(_", 10); // cannot print(a(3)
        ag.addGoal("test2");
        ag.assertPrint("a(10)", 10); // cannot print(a(3)
    }

}
