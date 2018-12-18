package jason.infra.jade;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

/**
 * This class implements the Jade version of the environment
 * infrastructure tier.
 *
 * @author Jomi
 */
@SuppressWarnings("serial")
public class JadeEnvironment extends JadeAg implements EnvironmentInfraTier {

    public static String actionOntology     = "AS-actions";
    public static String perceptionOntology = "AS-perception";

    private Environment userEnv;

    public JadeEnvironment() {
    }

    @Override
    public void setup()  {
        // create the user environment
        if (BaseCentralisedMAS.getRunner() != null)
            BaseCentralisedMAS.getRunner().setupLogger();
        logger.fine("Starting JadeEnvironment.");
        try {
            Object[] args = getArguments();
            if (args != null && args.length > 0) {
                if (args[0] instanceof ClassParameters) { // it is an mas2j parameter
                    ClassParameters ep = (ClassParameters)args[0];
                    userEnv = (Environment) Class.forName(ep.getClassName()).newInstance();
                    userEnv.setEnvironmentInfraTier(this);
                    userEnv.init(ep.getParametersArray());

                } else {
                    //args = args[0].toString().split(" ");
                    //for (Object o: args) System.out.println("*** "+o);
                    if (args[0].toString().equals("j-project")) { // load parameters from .mas2j
                        if (args.length != 2) {
                            logger.log(Level.SEVERE, "To start the environment from .mas2j file, you have to provide as parameters: (j-project,<file.mas2j>)");
                            return;
                        }
                        jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileReader(args[1].toString()));
                        MAS2JProject project = parser.mas();
                        project.setupDefault();

                        ClassParameters ep = project.getEnvClass();
                        userEnv = (Environment) Class.forName(ep.getClassName()).newInstance();
                        userEnv.setEnvironmentInfraTier(this);
                        userEnv.init(ep.getParametersArray());
                        logger.fine("Init of environmend, via j-project, done.");

                    } else { // assume first parameter as class name, remaining environment args
                        userEnv = (Environment) Class.forName(args[0].toString()).newInstance();
                        userEnv.setEnvironmentInfraTier(this);

                        String[] envArgs = new String[args.length-1];
                        for (int i=1; i<args.length; i++)
                            envArgs[i-1] = args[1].toString();
                        userEnv.init(envArgs);
                    }
                }
            } else {
                logger.warning("Using default environment.");
                userEnv = new Environment();
                userEnv.setEnvironmentInfraTier(this);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in setup Jade Environment", e);
        }

        // DF register
        DFAgentDescription dfa = new DFAgentDescription();
        dfa.setName(getAID());
        ServiceDescription vc = new ServiceDescription();
        vc.setType("jason");
        vc.setName(RunJadeMAS.environmentName);
        dfa.addServices(vc);
        try {
            DFService.register(this,dfa);
        } catch (FIPAException e) {
            logger.log(Level.SEVERE, "Error registering environment in DF", e);
        }
        logger.fine("Registry in the DF done.");

        try {
            // add a message handler to answer perception asks
            // and actions asks
            addBehaviour(new CyclicBehaviour() {
                ACLMessage m;
                public void action() {
                    m = receive();
                    if (m == null) {
                        block(1000);
                    } else {
                        // is getPerceps
                        if (m.getContent().equals("getPercepts")) {
                            ACLMessage r = m.createReply();
                            r.setPerformative(ACLMessage.INFORM);
                            try {
                                Collection<Literal> percepts = userEnv.getPercepts(m.getSender().getLocalName());
                                if (percepts == null) {
                                    r.setContent("nothing_new");
                                } else {
                                    synchronized (percepts) {
                                        r.setContent(percepts.toString());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            send(r);

                            // is action?
                        } else if (m.getOntology().equals(actionOntology)) {
                            ACLMessage r = m.createReply();
                            r.setPerformative(ACLMessage.INFORM);
                            try {
                                Structure action = Structure.parse(m.getContent());
                                userEnv.scheduleAction(m.getSender().getLocalName(), action, r);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            logger.fine("setup done");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agent", e);
        }
    }

    @Override
    protected void takeDown() {
        if (userEnv != null) userEnv.stop();
    }

    public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData) {
        try {
            ACLMessage r = (ACLMessage)infraData;
            if (success) {
                r.setContent("ok");
            } else {
                r.setContent("error");
            }
            send(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void informAgsEnvironmentChanged(String... agents) {
        if (agents.length == 0)
            broadcast(new Message("tell", null, null, "environmentChanged"));
        else
            informAgsEnvironmentChanged(Arrays.asList(agents));
    }

    public void informAgsEnvironmentChanged(Collection<String> agentsToNotify) {
        try {
            if (agentsToNotify == null) {
                informAgsEnvironmentChanged();
            } else {
                ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                m.setContent("environmentChanged");
                for (String ag: agentsToNotify) {
                    m.addReceiver(new AID(ag, AID.ISLOCALNAME));
                }
                send(m);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new JadeRuntimeServices(getContainerController(), this);
    }
}
