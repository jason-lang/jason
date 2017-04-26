package jason.infra.jade;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import jade.BootProfileImpl;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jason.JasonException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTerm;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.control.ExecutionControlGUI;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.MASConsoleGUI;
import jason.util.Config;


/**
 * Runs MASProject using JADE infrastructure.
 *
 * This class reads the mas2j project and create the
 * corresponding agents.
 *
 * @author Jomi
 */
public class RunJadeMAS extends RunCentralisedMAS {

    public static String controllerName  = "j_controller";
    public static String environmentName = "j_environment";

    private static Logger logger = Logger.getLogger(RunJadeMAS.class.getName());

    private AgentController envc, crtc;
    private Map<String,AgentController> ags = new HashMap<String,AgentController>();

    private ContainerController cc;

    private String targetContainer  = null; // start only agents of this container
    private ArrayList<String> initArgs   = new ArrayList<String>();
    private ProfileImpl profile; // profile used to start jade container

    public static void main(String[] args) throws JasonException {
        RunJadeMAS r = new RunJadeMAS();
        runner = r;
        r.init(args);
        r.create();
        r.start();
        r.waitEnd();
        r.finish();
    }



    public int init(String[] args) {
        // test if a container is informed
        for (int i=1; i<args.length; i++) {
            initArgs.add(args[i]);
            if (args[i].equals("-container-name")) {
                targetContainer = args[i+1];
            }
        }
        return super.init(args);
    }

    public void create() throws JasonException {
        if (startContainer()) {
            if (profile.getBooleanProperty(Profile.MAIN, true)) {
                createEnvironment();
                createController();
            }
            createAgs();
        }
    }

    public void addInitArgs(String[] args) {
        for (String a: args) {
            initArgs.addAll( Arrays.asList(a.split(" ")));
        }
        for (String a: initArgs) {
            if (a.equals("-sniffer")) {
                Config.get().put(Config.JADE_SNIFFER, "true");
            }
        }
    }

    public void createButtons() {
        createStopButton();
        createPauseButton();

        JButton btRMA = new JButton("Management Agent", new ImageIcon(jade.tools.sniffer.Sniffer.class.getResource("/jade/tools/sniffer/images/jadelogo.jpg"))); //"/jade/tools/rma/images/logosmall.jpg")));
        btRMA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    cc.createNewAgent("RMA", jade.tools.rma.rma.class.getName(), null).start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        });
        MASConsoleGUI.get().addButton(btRMA);

        JButton btSniffer = new JButton("JADE Sniffer", new ImageIcon(jade.tools.sniffer.Sniffer.class.getResource("/jade/tools/sniffer/images/sniffer.gif")));
        btSniffer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    cc.createNewAgent("Sniffer", jade.tools.sniffer.Sniffer.class.getName(), null).start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        });
        MASConsoleGUI.get().addButton(btSniffer);
    }


    public boolean startContainer() {
        try {
            // source based on jade.Boot
            try {
                String m = getProject().getInfrastructure().getParameter("main_container_host");
                if (m != null) {
                    Literal ml = ASSyntax.parseLiteral(m);
                    m = ((StringTerm)(ml.getTerm(0))).getString();
                    int pos = m.indexOf(":");
                    if (pos > 0) {
                        try {
                            initArgs.add("-port");
                            initArgs.add(m.substring(pos+1));
                            initArgs.add("-host");
                            initArgs.add(m.substring(0,pos));
                            initArgs.add("-container");
                            initArgs.add(m.substring(0,pos));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {}
            profile = new BootProfileImpl(prepareArgs( (String[])initArgs.toArray(new String[0])));
            //System.out.println(profile);
            if (profile.getBooleanProperty(Profile.MAIN, true)) {
                cc = Runtime.instance().createMainContainer(profile);
            } else {
                cc = Runtime.instance().createAgentContainer(profile);
                logger.info("Agent Container started with "+profile);
            }
            //Runtime.instance().setCloseVM(true); // Exit the JVM when there are no more containers around
            return cc != null;
        } catch (Throwable e) {
            logger.log(Level.WARNING,"Error starting JADE",e);
            return false;
        }
    }

    public void createEnvironment() throws JasonException {
        try {
            // create environment
            // the cartago + jade case
            if (JadeAgArch.isCartagoJadeCase(getProject())) {
                JadeAgArch.startCartagoNode(getProject().getEnvClass().getParametersArray());
            } else if (!getProject().getEnvClass().getClassName().equals(jason.environment.Environment.class.getName())){
                logger.info("Creating environment " + getProject().getEnvClass());
                envc = cc.createNewAgent(environmentName, JadeEnvironment.class.getName(), new Object[] { getProject().getEnvClass() });
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating the environment: ", e);
            return;
        }
    }

    public void createController() throws JasonException {
        try {
            // create controller
            ClassParameters controlClass = getProject().getControlClass();
            if (isDebug() && controlClass == null) {
                controlClass = new ClassParameters(ExecutionControlGUI.class.getName());
            }
            if (controlClass != null) {
                logger.fine("Creating controller " + controlClass);
                crtc = cc.createNewAgent(controllerName, JadeExecutionControl.class.getName(), new Object[] { controlClass });
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating the controller: ", e);
            return;
        }
    }


    public void createAgs() throws JasonException {
        try {
            // set the aslSrcPath in the include
            ((Include)DirectiveProcessor.getDirective("include")).setSourcePath(getProject().getSourcePaths());

            // create the agents
            for (AgentParameters ap : getProject().getAgents()) {
                try {
                    String agName = ap.name;
                    if (ap.getHost() != null && targetContainer != null && !ap.getHost().equals(targetContainer))
                        continue; // skip this agent, it is not for this container
                    if (ap.getHost() == null && !profile.getBooleanProperty(Profile.MAIN, true) && !Config.get().getClass().getName().equals("jacamo.util.Config"))
                        continue; // skip this agent, agents without host will be placed in the main container (but not in JaCaMo)

                    // mind inspector arch
                    if (ap.getOption("mindinspector") != null) {
                        ap.addArchClass( Config.get().getMindInspectorArchClassName());
                    }

                    for (int cAg = 0; cAg < ap.getNbInstances(); cAg++) {
                        String numberedAg = agName;
                        if (ap.getNbInstances() > 1)
                            numberedAg += (cAg + 1); //String.format("%0"+String.valueOf(ap.qty).length()+"d", cAg + 1);
                        logger.info("Creating agent " + numberedAg + " (" + (cAg + 1) + "/" + ap.getNbInstances() + ")");
                        AgentController ac = cc.createNewAgent(numberedAg, JadeAgArch.class.getName(), new Object[] { ap, isDebug(), getProject().getControlClass() != null });
                        ags.put(numberedAg,ac);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error creating agent " + ap.name, e);
                }
            }

            if (profile.getBooleanProperty(Profile.MAIN, true)) {
                // create rma
                if (Config.get().getBoolean(Config.JADE_RMA)) {
                    cc.createNewAgent("RMA", jade.tools.rma.rma.class.getName(), null).start();
                }

                // create sniffer
                if (Config.get().getBoolean(Config.JADE_SNIFFER)) {
                    cc.createNewAgent("Sniffer", jade.tools.sniffer.Sniffer.class.getName(), null).start();
                    Thread.sleep(1000); // give 1 second for sniffer to start
                }
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error creating agents: ", e);
        }
    }

    public void startAgs() {
        try {
            if (envc != null)
                envc.start();

            if (crtc != null)
                crtc.start();

            // run the agents
            for (AgentController ag : ags.values()) {
                ag.start();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agents.", e);
        }
    }

    public void finish() {
        try {
            logger.info("Finishing the system.");
            new JadeRuntimeServices(cc,null).stopMAS();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error stopping system.", e);
        }
        System.exit(0);
    }

    // CODE FROM jade.Boot

    /**
     * Transform original style boot arguments to new form.
     * <pre>
     * In the following 'x' and 'y' denote arbitrary strings; 'n' an integer.
     * Transformation Rules:
     * Original       New
     * ------------------------------
     * -host x        host:x
     * -owner x       owner:x
     * -name x        name:x
     * -port n        port:n
     * -mtp  x        mtp:x
     * -aclcodec:x    aclcodec:x
     * -conf x        import:x
     * -conf          -conf
     * -container     -container
     * -gui           -gui
     * -version       -version
     * -v             -version
     * -help          -help
     * -h             -help
     * -nomtp         -nomtp
     * -nomobility    -nomobility
     * -y x           y:x
     * agent list     agents:agent list
     * </pre>
     * If the arguments contain either import:x or agents:x
     * we will assume that the arguments are already in the new
     * format and leave them alone. For "import:" we test if
     * what follows is a file name and in the event it isn't we
     * assume that it was if there are any other "-x" options following.
     * <p>
     * You can't mix the old form with the new as this would make the
     * distinction between foo:bar as meaning a property named foo with
     * a value bar or an agent named foo implmented by class bar impossible.
     * <p>
     * @param args The command line arguments.
     */
    @SuppressWarnings("unchecked")
    protected String[] prepareArgs(String[] args) {
        boolean printUsageInfo = false;

        if ((args == null) || (args.length == 0)) {
            // printUsageInfo = true;
        } else {
            boolean isNew = false;
            boolean likely = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("import:")) {
                    int j = args[i].indexOf(':');
                    isNew = ( (j < args[i].length()-1) && (isFileName(args[i].substring(j+1))) );
                    likely = !isNew;  // in case malformed file name
                } else if (args[i].startsWith("agents:")) {
                    isNew = true;
                } else if (args[i].startsWith("-") && likely) {
                    isNew = true;
                }
            }

            if (isNew) {
                return args;
            }
        }

        int n = 0;
        boolean endCommand =
            false;    // true when there are no more options on the command line
        @SuppressWarnings("rawtypes")
        Vector results = new Vector();

        while ((n < args.length) &&!endCommand) {
            String theArg = args[n];

            if (theArg.equalsIgnoreCase("-conf")) {
                if (++n == args.length) {
                    // no modifier
                    results.add(theArg);
                } else {
                    // Use whatever is next as a candidate file name
                    String nextArg = args[n];
                    if (isFileName(nextArg)) {
                        // it was a file name
                        results.add("import:" + nextArg);
                    } else {
                        // its either an illformed file name or something else
                        results.add(theArg);
                        n--;
                    }
                }
            } else if (theArg.equalsIgnoreCase("-host")) {
                if (++n == args.length) {
                    System.err.println("Missing host name ");

                    printUsageInfo = true;
                } else {
                    results.add("host:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-owner")) {
                if (++n == args.length) {

                    // "owner:password" not provided on command line
                    results.add("owner:" + ":");

                } else {
                    results.add("owner:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-name")) {
                if (++n == args.length) {
                    System.err.println("Missing platform name");

                    printUsageInfo = true;
                } else {
                    results.add("name:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-container-name")) {
                if (++n == args.length) {
                    System.err.println("Missing container name");

                    printUsageInfo = true;
                } else {
                    results.add("container-name:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-imtp")) {
                if (++n == args.length) {
                    System.err.println("Missing IMTP class");

                    printUsageInfo = true;
                } else {
                    results.add("imtp:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-port")) {
                if (++n == args.length) {
                    System.err.println("Missing port number");

                    printUsageInfo = true;
                } else {
                    try {
                        Integer.parseInt(args[n]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Wrong int for the port number");

                        printUsageInfo = true;
                    }

                    results.add("port:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-container")) {
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase("-backupmain")) {
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase("-gui")) {
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase("-version")
                       || theArg.equalsIgnoreCase("-v")) {
                results.add("-version");
            } else if (theArg.equalsIgnoreCase("-help")
                       || theArg.equalsIgnoreCase("-h")) {
                results.add("-help");
            } else if (theArg.equalsIgnoreCase("-nomtp")) {
                results.add(theArg);
            } else if(theArg.equalsIgnoreCase("-nomobility")) {
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase(
                           "-dump")) {    // new form but useful for debugging
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase("-mtp")) {
                if (++n == args.length) {
                    System.err.println("Missing mtp specifiers");

                    printUsageInfo = true;
                } else {
                    results.add("mtp:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-aclcodec")) {
                if (++n == args.length) {
                    System.err.println("Missing aclcodec specifiers");

                    printUsageInfo = true;
                } else {
                    results.add("aclcodec:" + args[n]);
                }
            } else if (theArg.startsWith("-") && n+1 < args.length) {
                // Generic option
                results.add(theArg.substring(1)+":"+args[++n]);
            } else {
                endCommand = true;    //no more options on the command line
            }

            n++;    // go to the next argument
        }    // end of while

        // all options, but the list of Agents, have been parsed
        if (endCommand) {    // parse the list of agents, now
            --n;    // go to the previous argument

            StringBuffer sb = new StringBuffer();

            for (int i = n; i < args.length; i++) {
                sb.append(args[i] + " ");
            }

            results.add("agents:" + sb.toString());
        }

        if (printUsageInfo) {
            results.add("-help");
        }

        String[] newArgs = new String[results.size()];

        for (int i = 0; i < newArgs.length; i++) {
            newArgs[i] = (String) results.elementAt(i);
        }

        return newArgs;
    }

    /**
     * Test if an argument actually references a file.
     * @param arg The argument to test.
     * @return True if it does, false otherwise.
     */
    protected boolean isFileName(String arg) {
        File testFile = new File(arg);
        return testFile.exists();
    }
}
