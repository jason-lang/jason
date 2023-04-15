package test;

import jade.lang.acl.ACLMessage;
import jason.infra.jade.JadeAg;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class JadeAgTest extends TestCase {

    public void testKQMLtoACL() {
        assertEquals(JadeAg.kqmlToACL("tell").getPerformative(), ACLMessage.INFORM);
        assertEquals(JadeAg.aclPerformativeToKqml(JadeAg.kqmlToACL("tell")),"tell");

        assertEquals(JadeAg.aclPerformativeToKqml(new ACLMessage(ACLMessage.CFP)),"cfp");
        assertEquals(JadeAg.kqmlToACL(JadeAg.aclPerformativeToKqml(new ACLMessage(ACLMessage.CFP))).getPerformative(),ACLMessage.CFP);

        ACLMessage m = JadeAg.kqmlToACL("untell");
        assertEquals(ACLMessage.INFORM_REF, m.getPerformative());
        assertNotNull(m.getUserDefinedParameter("kqml-performative"));
        assertEquals(m.getUserDefinedParameter("kqml-performative"),"untell");

        assertEquals(JadeAg.aclPerformativeToKqml(m),"untell");

        assertEquals(ACLMessage.CFP, JadeAg.kqmlToACL("cfp").getPerformative());
        assertEquals(ACLMessage.ACCEPT_PROPOSAL, JadeAg.kqmlToACL("accept_proposal").getPerformative());
        assertEquals(ACLMessage.QUERY_IF, JadeAg.kqmlToACL("query_if").getPerformative());
        assertEquals(ACLMessage.PROPOSE, JadeAg.kqmlToACL("propose").getPerformative());
        assertEquals(ACLMessage.INFORM_IF, JadeAg.kqmlToACL("inform_if").getPerformative());
    }

}
