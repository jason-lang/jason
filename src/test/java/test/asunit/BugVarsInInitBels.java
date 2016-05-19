package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by Neil Madden -- see jason list */
public class BugVarsInInitBels {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
                "test_rule(A,A). "+ // do not replace A by _ (the test is just the name A for the var)
                "!test. "+
                "+!test <- A = test_wrong_value; "+
                "          ?test_rule(T,right_value); "+
                "          act(T)." // T should be 'right value'
        );
    }
    
    @Test(timeout=2000)
    public void testRule() {
        ag.assertAct("act(right_value)", 10); 
    }

}
