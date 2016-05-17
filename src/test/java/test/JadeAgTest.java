package test;

import jade.lang.acl.ACLMessage;
import jason.asSemantics.Message;
import jason.infra.jade.JadeAg;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class JadeAgTest extends TestCase {

    public void testKQMLtoACL() {
        assertEquals(JadeAg.kqmlToACL("tell").getPerformative(), ACLMessage.INFORM);
        assertEquals(JadeAg.aclToKqml(JadeAg.kqmlToACL("tell")),"tell");
        
        assertEquals(JadeAg.aclToKqml(new ACLMessage(ACLMessage.CFP)),"cfp");
        assertEquals(JadeAg.kqmlToACL(JadeAg.aclToKqml(new ACLMessage(ACLMessage.CFP))).getPerformative(),ACLMessage.CFP);
        
        ACLMessage m = JadeAg.kqmlToACL("untell");
        assertEquals(m.getPerformative(), ACLMessage.INFORM_REF);
        assertNotNull(m.getUserDefinedParameter("kqml-performative"));
        assertEquals(m.getUserDefinedParameter("kqml-performative"),"untell");
        
        assertEquals(JadeAg.aclToKqml(m),"untell");
    }

}
