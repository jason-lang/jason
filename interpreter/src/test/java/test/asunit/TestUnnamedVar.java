package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Test;

public class TestUnnamedVar {

    @Test(timeout=2000)
    public void testUnnamedVar() {
        TestAgent ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!test   <- A=a(B); !t(A); jason.asunit.print(A). "+
            "+!t(A) <- B=3. "
        );
        ag.addGoal("test");
        ag.assertPrint("a(B)", 10); // cannot print(a(3)
    }

}
