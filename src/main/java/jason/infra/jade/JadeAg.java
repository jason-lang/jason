package jason.infra.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jason.asSemantics.Message;
import jason.asSyntax.Term;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of a basic jade agent for jason agents
 * 
 * @author Jomi
 */
public abstract class JadeAg extends Agent {

    // KQML performatives not available in FIPA-ACL
    public static final int UNTELL    = 1001;
    public static final int ASKALL    = 1002;
    public static final int UNACHIEVE = 1003;
    public static final int TELLHOW   = 1004;
    public static final int UNTELLHOW = 1005;
    public static final int ASKHOW    = 1006;
    
    private static final long serialVersionUID = 1L;

    protected Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());

    private static int rwid = 0; // reply-with counter
    protected boolean running = true;
    
    protected Map<String,String> conversationIds = new HashMap<String,String>();
    
    @Override
    public void doDelete() {
        running = false;
        super.doDelete();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int incReplyWithId() {
        return rwid++;
    }
    
    public void sendMsg(Message m) throws Exception {
        ACLMessage acl = jasonToACL(m);
        acl.addReceiver(new AID(m.getReceiver(), AID.ISLOCALNAME));
        if (m.getInReplyTo() != null) {
            String convid = conversationIds.get(m.getInReplyTo());
            if (convid != null) {
                acl.setConversationId(convid);
            }
        }
        if (logger.isLoggable(Level.FINE)) logger.fine("Sending message: " + acl);
        send(acl);
    }

    public void broadcast(final Message m) {
        addBehaviour(new OneShotBehaviour() {
            private static final long serialVersionUID = 1L;
            public void action() {
                try {
                    ACLMessage acl = jasonToACL(m);
                    addAllAgsAsReceivers(acl);
                    send(acl);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error in broadcast of "+m, e);
                }
            }
        });
    }
    
    public void putConversationId(String replyWith, String mId) {
        conversationIds.put(replyWith, mId);
    }
    
    protected ACLMessage ask(final ACLMessage m) {
        try {
            String waitingRW = "id"+incReplyWithId();
            m.setReplyWith(waitingRW);
            send(m);                    
            ACLMessage r = blockingReceive(MessageTemplate.MatchInReplyTo(waitingRW), 5000);
            if (r != null) 
                return r;
            else 
                logger.warning("ask timeout for "+m.getContent());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error waiting message.", e);            
        }
        return null;
    }

    
    public void addAllAgsAsReceivers(ACLMessage m) throws Exception {
        // get all agents' name
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("jason");
        sd.setName(JadeAgArch.dfName);
        template.addServices(sd);
        DFAgentDescription[] ans = DFService.search(this, template);
        for (int i=0; i<ans.length; i++) {
            if (!ans[i].getName().equals(getAID())) {
                m.addReceiver(ans[i].getName());
            }
        }
    }

    protected ACLMessage jasonToACL(Message m) throws IOException {
        ACLMessage acl = kqmlToACL(m.getIlForce());
        // send content as string if it is a Term/String (it is better for interoperability)
        if (m.getPropCont() instanceof Term || m.getPropCont() instanceof String) {
            acl.setContent(m.getPropCont().toString());         
        } else {
            acl.setContentObject((Serializable)m.getPropCont());
        }
        acl.setReplyWith(m.getMsgId());
        acl.setLanguage("AgentSpeak");
        if (m.getInReplyTo() != null) {
            acl.setInReplyTo(m.getInReplyTo());
        }
        return acl;
    }
    
    public static ACLMessage kqmlToACL(String p) {
        if (p.equals("tell")) {
            return new ACLMessage(ACLMessage.INFORM);
        } else if (p.equals("askOne")) {
            return new ACLMessage(ACLMessage.QUERY_REF);
        } else if (p.equals("achieve")) {
            return new ACLMessage(ACLMessage.REQUEST);
        } else if (p.equals("untell") || 
                   p.equals("unachieve") ||
                   p.equals("askAll") ||
                   p.equals("askHow") ||
                   p.equals("tellHow") ||
                   p.equals("untellHow")) {
            ACLMessage m = new ACLMessage(ACLMessage.INFORM_REF);
            m.addUserDefinedParameter("kqml-performative", p);
            return m;
        /*} else if (p.equals("unachieve")) {
            return UNACHIEVE;
        } else if (p.equals("askAll")) {
            return ASKALL;
        } else if (p.equals("askHow")) {
            return ASKHOW;
        } else if (p.equals("tellHow")) {
            return TELLHOW;
        } else if (p.equals("untellHow")) {
            return UNTELLHOW;*/
        }
        return new ACLMessage(ACLMessage.getInteger(p));            
    }
    
    public static String aclToKqml(ACLMessage m) {
        switch(m.getPerformative()) {
        case ACLMessage.INFORM: return "tell"; 
        case ACLMessage.QUERY_REF: return "askOne";
        case ACLMessage.REQUEST: return "achieve";
        /*case UNTELL: return "untell";
        case UNACHIEVE: return "unachieve";
        case ASKALL: return "askAll";
        case ASKHOW: return "askHow";
        case TELLHOW: return "tellHow";
        case UNTELLHOW: return "untellHow";*/
        case ACLMessage.INFORM_REF: 
            String kp = m.getUserDefinedParameter("kqml-performative");
            if (kp != null) {
                return kp;
            }
            break;
        }
        return ACLMessage.getPerformative(m.getPerformative()).toLowerCase().replaceAll("-", "_");    
    }
    
}
