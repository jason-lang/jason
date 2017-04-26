package jason.infra.jade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.mas2j.AgentParameters;
import jason.runtime.RuntimeServicesInfraTier;

public class JasonBridgeArch extends AgArch {

    JadeAgArch jadeAg;
    AID environmentAID = null;
    Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());

    // map of pending actions
    private Map<String,ActionExec> myPA = new HashMap<String,ActionExec>();


    public JasonBridgeArch(JadeAgArch jadeAg) {
        this.jadeAg = jadeAg;
        logger = jade.util.Logger.getMyLogger(this.getClass().getName() + "." + getAgName());
    }

    public void init(AgentParameters ap) throws Exception {
        Agent.create(this, ap.agClass.getClassName(), ap.getBBClass(), ap.asSource.getAbsolutePath(), ap.getAsSetts(false, false));
        insertAgArch(this);
        createCustomArchs(ap.getAgArchClasses());

        if (getTS().getSettings().verbose() >= 0)
            logger.setLevel(getTS().getSettings().logLevel());
    }

    /*@Override
    public void sleep() {
        jadeAg.enterInSleepMode();
        //tsBehaviour.block(1000);
    }*/

    @Override
    public void wake() {
        jadeAg.wakeUp();
    }

    @Override
    public void stop() {
        getTS().getAg().stopAg();
        super.stop();
    }

    @Override
    public String getAgName() {
        return jadeAg.getLocalName();
    }

    @Override
    public boolean canSleep() {
        return jadeAg.getCurQueueSize() == 0 && isRunning();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Literal> perceive() {
        super.perceive();

        if (!isRunning()) return null;
        if (getEnvironmentAg() == null) return null;

        @SuppressWarnings("rawtypes")
        List percepts = null;
        try {
            ACLMessage askMsg = new ACLMessage(ACLMessage.QUERY_REF);
            askMsg.addReceiver(environmentAID);
            askMsg.setOntology(JadeEnvironment.perceptionOntology);
            askMsg.setContent("getPercepts");
            ACLMessage r = jadeAg.ask(askMsg);
            if (r != null && r.getContent().startsWith("[")) {
                percepts = ListTermImpl.parseList(r.getContent());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in perceive.", e);
        }

        return percepts;
    }

    public JadeAgArch getJadeAg() {
        return jadeAg;
    }



    @Override
    public void sendMsg(Message m) throws Exception {
        jadeAg.sendMsg(m);
    }

    @Override
    public void broadcast(Message m) throws Exception {
        jadeAg.broadcast(m);
    }

    @Override
    public void checkMail() {
        ACLMessage m = null;
        do {
            try {
                m = jadeAg.receive();
                if (m != null) {
                    if (logger.isLoggable(Level.FINE)) logger.fine("Received message: " + m);

                    if (isActionFeedback(m)) {
                        // ignore this message
                        continue;
                    }

                    String ilForce   = JadeAg.aclPerformativeToKqml(m);
                    String sender    = m.getSender().getLocalName();
                    String replyWith = m.getReplyWith();
                    String irt       = m.getInReplyTo();

                    // also remembers conversation ID
                    if (replyWith != null && replyWith.length() > 0) {
                        if (m.getConversationId() != null) {
                            jadeAg.putConversationId(replyWith, m.getConversationId());
                        }
                    } else {
                        replyWith = "noid";
                    }

                    Object propCont = translateContentToJason(m);
                    if (propCont != null) {
                        jason.asSemantics.Message im = new jason.asSemantics.Message(ilForce, sender, getAgName(), propCont, replyWith);
                        if (irt != null) {
                            im.setInReplyTo(irt);
                        }
                        getTS().getC().getMailBox().add(im);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error receiving message.", e);
            }
        } while (m != null);
    }

    /** returns the content of the message m and implements some pro-processing of the content, if necessary */
    protected Object translateContentToJason(ACLMessage m) {
        Object propCont = null;
        try {
            propCont = m.getContentObject();
            if (propCont instanceof String) {
                // try to parse as term
                try {
                    propCont = ASSyntax.parseTerm((String)propCont);
                } catch (Exception e) {  // no problem
                }
            }
        } catch (UnreadableException e) { // no problem try another thing
        }

        if (propCont == null) { // still null
            // try to parse as term
            try {
                propCont = ASSyntax.parseTerm(m.getContent());
            } catch (Exception e) {
                // not AS messages are treated as string
                propCont = new StringTermImpl(m.getContent());
            }
        }
        return propCont;
    }


    @Override
    public void act(ActionExec action) {
        if (!isRunning()) return;
        if (getEnvironmentAg() == null) return;

        try {
            Term acTerm = action.getActionTerm();
            logger.fine("doing: " + acTerm);

            String rw  = "id"+jadeAg.incReplyWithId();
            ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
            m.addReceiver(environmentAID);
            m.setOntology(JadeEnvironment.actionOntology);
            m.setContent(acTerm.toString());
            m.setReplyWith(rw);
            myPA.put(rw, action);
            jadeAg.send(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending action " + action, e);
        }
    }

    @Override
    public RuntimeServicesInfraTier getRuntimeServices() {
        return new JadeRuntimeServices(jadeAg.getContainerController(), jadeAg);
    }

    private boolean consultEnv = false;
    private AID getEnvironmentAg() {
        // get the name of the environment
        if (!consultEnv) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("jason");
            sd.setName(RunJadeMAS.environmentName);
            template.addServices(sd);
            try {
                DFAgentDescription[] ans = DFService.search(jadeAg, template);
                if (ans.length > 0) {
                    environmentAID =  ans[0].getName();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error getting environment from DF.",e);
            }
            consultEnv = true;
        }
        return environmentAID;
    }

    boolean isActionFeedback(ACLMessage m) {
        // check if there are feedbacks on requested action executions
        if (m.getOntology() != null && m.getOntology().equals(JadeEnvironment.actionOntology)) {
            String irt = m.getInReplyTo();
            if (irt != null) {
                ActionExec a = myPA.remove(irt);
                // was it a pending action?
                if (a != null) {
                    a.setResult(m.getContent().equals("ok"));
                    actionExecuted(a);
                } else {
                    logger.log(Level.SEVERE, "Error: received feedback for an Action that is not pending. The message is "+m);
                }
            }
            return true;
        }
        return false;
    }
}
