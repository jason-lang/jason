package jason.infra.jade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;

import org.w3c.dom.Document;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;


/**
 * Implementation of the Jade Architecture to run Jason agents
 *
 * @author Jomi
 */
public class JadeAgArch extends JadeAg {

    /** name of the "jason agent" service in DF */
    public  static String dfName = "j_agent";

    private static final long serialVersionUID = 1L;

    protected JasonBridgeArch jasonBridgeAgArch;

    //private boolean enterInSleepMode = false;

    AID controllerAID  = new AID(RunJadeMAS.controllerName, AID.ISLOCALNAME);

    Behaviour tsBehaviour;

    //
    // Jade Methods
    // ------------
    //

    @SuppressWarnings("serial")
    @Override
    protected void setup() {
        if (BaseCentralisedMAS.getRunner() != null)
            BaseCentralisedMAS.getRunner().setupLogger();
        logger = jade.util.Logger.getMyLogger(this.getClass().getName() + "." + getLocalName());
        logger.info("starting "+getLocalName());
        try {
            AgentParameters ap = parseParameters();
            if (ap != null) {
                jasonBridgeAgArch = new JasonBridgeArch(this);
                jasonBridgeAgArch.init(ap);

                if (jasonBridgeAgArch.getTS().getSettings().verbose() >= 0)
                    logger.setLevel(jasonBridgeAgArch.getTS().getSettings().logLevel());

                registerAgInDF();

                tsBehaviour = new JasonTSReasoner();
                addBehaviour(tsBehaviour);

                logger.fine("Created from source "+ap.asSource);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error creating JADE architecture.",e);
        }
    }

    /*
    @Override
    protected void afterMove() {
        super.afterMove();
    }
    */

    /*void enterInSleepMode() {
        enterInSleepMode = true;
    }*/

    void wakeUp() {
        if (tsBehaviour != null) // it can happen that the setup was not run before this method...
            tsBehaviour.restart();
    }

    protected AgentParameters parseParameters() throws ParseException, IOException {

        Object[] args = getArguments();
        if (args == null || args.length == 0) {
            logger.info("No AgentSpeak source informed!");
            return null;
        }

        // read arguments
        // if [0] is an instance of AgentParameters
        //    read parameters from [0]
        // else if [0] is j-project
        //    read all parameters form [1] (including aslSource and directives)
        //    create the agent indicated by [2]
        // else
        //    [0] is the file with AS source for the agent
        //    arch <arch class>
        //    ag <agent class>
        //    bb < belief base class >
        //    option < options >
        if (args[0] instanceof AgentParameters) {
            return (AgentParameters)args[0];
        } else {
            if (args[0].toString().equals("j-project")) { // load parameters from .mas2j
                if (args.length != 3) {
                    logger.log(Level.SEVERE, "To start agents from .mas2j file, you have to provide as parameters: (j-project,<file.mas2j>,<nameofagent in mas2j>)");
                    logger.log(Level.SEVERE, "Current parameters are:");
                    for (int i=0; i<args.length; i++) {
                        logger.log(Level.SEVERE, "   "+i+" = "+args[i]);                       
                    }
                    return null;
                }
                jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileReader(args[1].toString()));
                MAS2JProject project = parser.mas();
                project.setupDefault();

                project.registerDirectives();
                ((Include)DirectiveProcessor.getDirective("include")).setSourcePath(project.getSourcePaths());

                AgentParameters ap = project.getAg(args[2].toString());
                if (ap == null) {
                    logger.log(Level.SEVERE, "There is no agent '"+args[2]+"' in project '"+args[1]+"'.");
                } else {
                    ap.asSource = new File(project.getSourcePaths().fixPath(ap.asSource.toString()));
                    //if (ap.qty > 1)
                    //    logger.warning("Ignoring quantity of agents from mas2j, jade arch creates only ONE agent.");
                }

                // The case CARTAGO+JADE
                if (isCartagoJadeCase(project)) {
                    startCartagoNode(project.getEnvClass().getParametersArray());
                }
                return ap;

            } else { // load parameters from shell
                AgentParameters ap = new AgentParameters();
                ap.asSource = new File(args[0].toString());

                int i=1;
                while (i < args.length) {
                    if (args[i].toString().equals("arch")) {
                        i++;
                        ap.addArchClass(args[i].toString());
                    } else if (args[i].toString().equals("ag")) {
                        i++;
                        ap.agClass = new ClassParameters(args[i].toString());
                    }
                    i++;
                }
                return ap;
            }
        }
    }

    public static boolean isCartagoJadeCase(MAS2JProject project) {
        return
            project.getEnvClass() != null &&
            project.getEnvClass().getClassName().equals("c4jason.CartagoEnvironment") &&
            project.isJade();
    }

    private static boolean cartagoStarted = false;
    public static synchronized void startCartagoNode(String[] args) {
        if (!cartagoStarted) {
            System.out.print("Starting cartago node....");
            //CartagoEnvironment env = new CartagoEnvironment();
            //env.init(args);
            //System.out.println("ok.");
            System.out.println("**** not implemented!!!! ****");
        }
        cartagoStarted = true;
    }

    private void registerAgInDF() {
        // DF register
        DFAgentDescription dfa = new DFAgentDescription();
        dfa.setName(getAID());
        ServiceDescription vc = new ServiceDescription();
        vc.setType("jason");
        vc.setName(dfName);
        dfa.addServices(vc);
        try {
            DFService.register(this,dfa);
        } catch (FIPAException e) {
            try {
                DFService.deregister(this);
                DFService.register(this,dfa);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error registering agent in DF", e);
                logger.log(Level.SEVERE, "Error unregistering agent in DF", ex);
            }
        }
    }

    class JasonTSReasoner extends CyclicBehaviour {
        TransitionSystem ts = jasonBridgeAgArch.getTS();
        public void action() {
            if (ts.getSettings().isSync()) {
                if (processExecutionControlOntologyMsg()) {
                    // execute a cycle in sync mode
                    ts.reasoningCycle();
                    boolean isBreakPoint = false;
                    try {
                        isBreakPoint = ts.getC().getSelectedOption().getPlan().hasBreakpoint();
                        if (logger.isLoggable(Level.FINE)) logger.fine("Informing controller that I finished a reasoning cycle "+jasonBridgeAgArch.getCycleNumber()+". Breakpoint is " + isBreakPoint);
                    } catch (NullPointerException e) {
                        // no problem, there is no sel opt, no plan ....
                    }
                    informCycleFinished(isBreakPoint, jasonBridgeAgArch.getCycleNumber());

                } else {
                    block(1000);
                }
            } else {
                ts.reasoningCycle();
                if (ts.canSleep()) {
                    block(1000);
                    //enterInSleepMode = false;
                }
            }
        }
    }

    @Override
    public void doDelete() {
        try {
            running = false;
            if (jasonBridgeAgArch != null) {
                jasonBridgeAgArch.getFirstAgArch().stop();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in doDelete.",e);
        } finally {
            super.doDelete();
        }
    }

    @Override
    protected void takeDown() {
        logger.info("Finished!");
    }


    private MessageTemplate ts = MessageTemplate.and(
                                     MessageTemplate.MatchContent("agState"),
                                     MessageTemplate.MatchOntology(JadeExecutionControl.controllerOntology));
    private MessageTemplate tc = MessageTemplate.and(
                                     MessageTemplate.MatchContent("performCycle"),
                                     MessageTemplate.MatchOntology(JadeExecutionControl.controllerOntology));

    boolean processExecutionControlOntologyMsg() {
        ACLMessage m = receive(ts);
        if (m != null) {
            // send the agent state
            ACLMessage r = m.createReply();
            r.setPerformative(ACLMessage.INFORM);
            try {
                Document agStateDoc = jasonBridgeAgArch.getTS().getAg().getAgState();
                r.setContentObject((Serializable)agStateDoc);
                send(r);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error sending message " + r, e);
            }
        }

        m = receive(tc);
        if (m != null) {
            int cycle = Integer.parseInt(m.getUserDefinedParameter("cycle"));
            logger.fine("new cycle: "+cycle);
            jasonBridgeAgArch.setCycleNumber(cycle);
            return true;
        }
        return false;
    }

    /*
    boolean isPerceptionOntology(ACLMessage m) {
        return m.getOntology() != null && m.getOntology().equals(JadeEnvironment.perceptionOntology);
    }
    */


    /**
     *  Informs the infrastructure tier controller that the agent
     *  has finished its reasoning cycle (used in sync mode).
     *
     *  <p><i>breakpoint</i> is true in case the agent selected one plan
     *  with the "breakpoint" annotation.
     */
    public void informCycleFinished(boolean breakpoint, int cycle) {
        try {
            ACLMessage m = new ACLMessage(ACLMessage.INFORM);
            m.addReceiver(controllerAID);
            m.setOntology(JadeExecutionControl.controllerOntology);
            m.setContent(breakpoint+","+cycle);
            send(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending cycle finished.", e);
        }
    }
}
