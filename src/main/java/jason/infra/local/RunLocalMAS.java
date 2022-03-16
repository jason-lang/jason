package jason.infra.local;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.management.ObjectName;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.bb.DefaultBeliefBase;
import jason.control.ExecutionControlGUI;
import jason.infra.components.CircumstanceListenerComponents;
import jason.infra.repl.ReplAgGUI;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.runtime.MASConsoleGUI;
import jason.runtime.MASConsoleLogFormatter;
import jason.runtime.MASConsoleLogHandler;
import jason.runtime.RuntimeServices;
import jason.runtime.RuntimeServicesFactory;
import jason.runtime.Settings;
import jason.runtime.SourcePath;
import jason.util.Config;

/**
 * Runs MASProject using Local infrastructure.
 */
public class RunLocalMAS extends BaseLocalMAS implements RunLocalMASMBean {

    private JButton  btDebug;
    protected boolean  isRunning = false;

    protected List<LocalAgArch> createdAgents = new ArrayList<>();

    public RunLocalMAS() {
        super();
        if (RuntimeServicesFactory.get() == null)
            RuntimeServicesFactory.set( new LocalRuntimeServices(this) );
        runner = this;
    }

    public boolean hasDebugControl() {
        return btDebug != null;
    }

    public void enableDebugControl() {
        btDebug.setEnabled(true);
    }

    public static void main(String[] args) throws JasonException {
        RunLocalMAS r = new RunLocalMAS();
        runner = r;
        r.init(args);
        r.registerMBean();
        r.create();
        r.start();
        r.waitEnd();
        r.finish(0, true, 0);
    }

    protected void registerMBean() {
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName("jason.sf.net:type=runner"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected int init(String[] args) {
        String projectFileName = null;
        if (args.length < 1) {
            if (RunLocalMAS.class.getResource("/"+defaultProjectFileName) != null) {
                projectFileName = defaultProjectFileName;
                appFromClassPath = true;
                Config.get(false); // to void to call fix/store the configuration in this case everything is read from a jar/jnlp file
            } else {
                System.out.println("Jason "+Config.get().getJasonVersion());
                System.err.println("You should inform the MAS project file.");
                //JOptionPane.showMessageDialog(null,"You should inform the project file as a parameter.\n\nJason version "+Config.get().getJasonVersion()+" library built on "+Config.get().getJasonBuiltDate(),"Jason", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        } else {
            projectFileName = args[0];
        }

        if (Config.get().getJasonJar() == null) {
            //System.out.println("Jason is not configured, creating a default configuration");
            Config.get().setShowFixMsgs(false);
            Config.get().fix();
        }

        Map<String,Object> mArgs = parseArgs(args);

        setupLogger((String)mArgs.get("log-conf"));

        if ((boolean)(mArgs.getOrDefault("debug", false))) {
            debug = true;
            Logger.getLogger("").setLevel(Level.FINE);
        }

        // discover the handler
        for (Handler h : Logger.getLogger("").getHandlers()) {
            // if there is a MASConsoleLogHandler, show it
            if (h.getClass().toString().equals(MASConsoleLogHandler.class.toString())) {
                MASConsoleGUI.get().getFrame().setVisible(true);
                MASConsoleGUI.get().setAsDefaultOut();
            }
        }

        int errorCode = 0;

        try {
            String urlPrefix = null;
            if (projectFileName != null) {
                InputStream inProject;
                if (appFromClassPath) {
                    inProject = RunLocalMAS.class.getResource("/"+defaultProjectFileName).openStream();
                    urlPrefix = SourcePath.CRPrefix;
                } else {
                    URL file;
                    // test if the argument is an URL
                    try {
                        projectFileName = new SourcePath().fixPath(projectFileName); // replace $jasonJar, if necessary
                        file = new URL(projectFileName);
                        if (projectFileName.startsWith("jar")) {
                            urlPrefix = projectFileName.substring(0,projectFileName.indexOf("!")+1);
                        }
                    } catch (Exception e) {
                        file = new URL("file:"+projectFileName);
                    }
                    inProject = file.openStream();
                }
                jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(inProject);
                project = parser.mas();
            } else {
                project = new MAS2JProject();
            }

            project.setupDefault();
            project.getSourcePaths().addPath(urlPrefix);
            project.registerDirectives();
            // set the aslSrcPath in the include
            ((Include)DirectiveProcessor.getDirective("include")).setSourcePath(project.getSourcePaths());

            project.fixAgentsSrc();

            if (MASConsoleGUI.hasConsole()) {
                MASConsoleGUI.get().setTitle("MAS Console - " + project.getSocName());

                createButtons();
            }

            //runner.waitEnd();
            errorCode = 0;

        } catch (FileNotFoundException e1) {
            logger.log(Level.SEVERE, "File " + projectFileName + " not found!");
            errorCode = 2;
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "Error parsing file " + projectFileName + "!", e);
            errorCode = 3;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error!?: ", e);
            errorCode = 4;
        }

        System.out.flush();
        System.err.flush();

        if (!MASConsoleGUI.hasConsole() && errorCode != 0) {
            System.exit(errorCode);
        }
        return errorCode;
    }

    protected Map<String,Object> parseArgs(String[] args) {
        Map<String, Object> margs = new HashMap<>();

        if (args.length > 0) {
            String la = "";
            for (String arg: args) {
                if (la.equals("--log-conf")) {
                    margs.put("log-conf", arg);
                }
                if (arg.equals("--debug") || arg.equals("-d"))
                    margs.put("debug", true);

                la = arg;
            }
        }

        return margs;
    }

    /** create environment, agents, controller */
    protected void create() throws JasonException {
        createEnvironment();
        createAgs();
        createController();
    }

    /** start agents, .... */
    protected void start() {
        isRunning = true;
        startAgs();
        startSyncMode();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setupLogger() {
        setupLogger(null);
    }

    public synchronized void setupLogger(String confFile) {
        if (appFromClassPath) {
            try {
                LogManager.getLogManager().readConfiguration(
                        RunLocalMAS.class.getResource("/"+logPropFile).openStream());
            } catch (Exception e) {
                Handler[] hs = Logger.getLogger("").getHandlers();
                for (int i = 0; i < hs.length; i++) {
                    Logger.getLogger("").removeHandler(hs[i]);
                }
                Handler h = new MASConsoleLogHandler();
                h.setFormatter(new MASConsoleLogFormatter());
                Logger.getLogger("").addHandler(h);
                Logger.getLogger("").setLevel(Level.INFO);
            }
        } else if (confFile != null && (confFile.startsWith("jar:") || confFile.startsWith("$"))) {
            try {
                confFile = new SourcePath().fixPath(confFile);
                URL logurl = new URL(confFile);
                LogManager.getLogManager().readConfiguration( logurl.openStream() );
                System.out.println("logging configuration was loaded from "+logurl);
            } catch (Exception e) {
                System.err.println("Error setting up logger:" + e);
                e.printStackTrace();
            }

        } else {
            if (confFile == null) {
                confFile = logPropFile;
            } else {
                if (!(new File(confFile).exists()))
                   System.err.println("Loggging properties file "+confFile+" not found!");
            }

            // checks a local log configuration file
            if (new File(confFile).exists()) {
                try {
                    LogManager.getLogManager().readConfiguration(new FileInputStream(confFile));
                } catch (Exception e) {
                    System.err.println("Error setting up logger:" + e);
                }
            } else {
                try {
                    if (runner != null) {
                        LogManager.getLogManager().readConfiguration(getDefaultLogProperties());
                    } else {
                        LogManager.getLogManager().readConfiguration(RunLocalMAS.class.getResource("/templates/" + logPropFile).openStream());
                    }
                } catch (Exception e) {
                    System.err.println("Error setting up logger:" + e);
                    e.printStackTrace();
                }
            }
        }
    }

    protected InputStream getDefaultLogProperties() throws IOException {
        return RunLocalMAS.class.getResource("/templates/" + logPropFile).openStream();
    }

    protected void setupDefaultConsoleLogger() {
        Handler[] hs = Logger.getLogger("").getHandlers();
        for (int i = 0; i < hs.length; i++) {
            Logger.getLogger("").removeHandler(hs[i]);
        }
        Handler h = new ConsoleHandler();
        h.setFormatter(new MASConsoleLogFormatter());
        Logger.getLogger("").addHandler(h);
        Logger.getLogger("").setLevel(Level.INFO);
    }

    protected void createButtons() {
        createStopButton();

        // add Button pause
        createPauseButton();

        // add Button debug
        btDebug = new JButton("Debug", new ImageIcon(RunLocalMAS.class.getResource("/images/debug.gif")));
        btDebug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                changeToDebugMode();
                btDebug.setEnabled(false);
                if (runner.control != null) {
                    try {
                        runner.control.getUserControl().setRunningCycle(false);
                    } catch (Exception e) { }
                }
            }
        });
        if (debug) {
            btDebug.setEnabled(false);
        }
        MASConsoleGUI.get().addButton(btDebug);

        // add Button start
        final JButton btStartAg = new JButton("New agent", new ImageIcon(RunLocalMAS.class.getResource("/images/newAgent.gif")));
        btStartAg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new StartNewAgentGUI(MASConsoleGUI.get().getFrame(), "Start a new agent to run in current MAS", System.getProperty("user.dir"));
            }
        });
        MASConsoleGUI.get().addButton(btStartAg);

        // add Button kill
        final JButton btKillAg = new JButton("Kill agent", new ImageIcon(RunLocalMAS.class.getResource("/images/killAgent.gif")));
        btKillAg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new KillAgentGUI(MASConsoleGUI.get().getFrame(), "Kill an agent of the current MAS");
            }
        });
        MASConsoleGUI.get().addButton(btKillAg);

        createNewReplAgButton();

        // add show sources button
        final JButton btShowSrc = new JButton("Sources", new ImageIcon(RunLocalMAS.class.getResource("/images/list.gif")));
        btShowSrc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showProjectSources(project);
            }
        });
        MASConsoleGUI.get().addButton(btShowSrc);

    }

    protected void createPauseButton() {
        final JButton btPause = new JButton("Pause", new ImageIcon(RunLocalMAS.class.getResource("/images/resume_co.gif")));
        btPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (MASConsoleGUI.get().isPause()) {
                    btPause.setText("Pause");
                    MASConsoleGUI.get().setPause(false);
                } else {
                    btPause.setText("Continue");
                    MASConsoleGUI.get().setPause(true);
                }

            }
        });
        MASConsoleGUI.get().addButton(btPause);
    }

    protected void createStopButton() {
        // add Button
        JButton btStop = new JButton("Stop", new ImageIcon(RunLocalMAS.class.getResource("/images/suspend.gif")));
        btStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                MASConsoleGUI.get().setPause(false);
                runner.finish(0, true, 0);
            }
        });
        MASConsoleGUI.get().addButton(btStop);
    }

    protected void createNewReplAgButton() {
        // add Button debug
        final JButton btStartAg = new JButton("New REPL agent", new ImageIcon(RunLocalMAS.class.getResource("/images/newAgent.gif")));
        btStartAg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final JFrame f = new JFrame("New REPL Agent, give it a name");
                //f.getContentPane().setLayout(new BorderLayout());
                //f.getContentPane().add(BorderLayout.NORTH,command);
                //f.getContentPane().add(BorderLayout.CENTER,mindPanel);
                final JTextField n = new JTextField(30);
                n.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        f.setVisible(false);
                        createReplAg(n.getText());
                    }
                });
                f.setLayout(new FlowLayout());
                f.add(n);
                f.pack();
                f.setLocation((int)btStartAg.getLocationOnScreen().x, (int)btStartAg.getLocationOnScreen().y+30);
                f.setVisible(true);
            }
        });
        MASConsoleGUI.get().addButton(btStartAg);
    }

    protected void createReplAg(String n) {
        LocalAgArch agArch = new LocalAgArch();
        try {
            agArch.setAgName(n);
            agArch.setEnvInfraTier(env);
            agArch.createArchs(null, ReplAgGUI.class.getName(), null, null, new Settings());
            Thread agThread = new Thread(agArch);
            agArch.setThread(agThread);
            agThread.start();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        addAg(agArch);
    }


    protected void createEnvironment() throws JasonException {
        if (project.getEnvClass() != null && !project.getEnvClass().getClassName().equals(jason.environment.Environment.class.getName())) {
            logger.fine("Creating environment " + project.getEnvClass());
            env = new LocalEnvironment(project.getEnvClass(), this);
        }
    }


    protected void createAgs() throws JasonException {

        RConf generalConf = RConf.fromString(project.getInfrastructure().getParameter(0));

        int nbAg = 0;
        Agent pag = null;

        RuntimeServices rs = RuntimeServicesFactory.get();

        // create agents
        for (AgentParameters ap : project.getAgents()) {
            try {

                String agName = ap.name;

                for (int cAg = 0; cAg < ap.getNbInstances(); cAg++) {
                    nbAg++;

                    String numberedAg = agName;
                    if (ap.getNbInstances() > 1) {
                        numberedAg += (cAg + 1);
                        // cannot add zeros before, it causes many compatibility problems and breaks dynamic creation
                        // numberedAg += String.format("%0"+String.valueOf(ap.qty).length()+"d", cAg + 1);
                    }

                    numberedAg = rs.getNewAgentName(numberedAg);

                    ap.addArchClass(rs.getDefaultAgArchs());
                    logger.fine("Creating agent " + numberedAg + " (" + (cAg + 1) + "/" + ap.getNbInstances() + ")");

                    RConf agentConf;
                    if (ap.getOption("rc") == null) {
                        agentConf = generalConf;
                    } else {
                        agentConf = RConf.fromString(ap.getOption("rc"));
                    }

                    // Get the number of reasoning cycles or number of cycles for each stage
                    int cycles           = -1; // -1 means default value of the platform
                    int cyclesSense      = -1;
                    int cyclesDeliberate = -1;
                    int cyclesAct        = -1;

                    if (ap.getOption("cycles") != null) {
                        cycles = Integer.valueOf(ap.getOption("cycles"));
                    }
                    if (ap.getOption("cycles_sense") != null) {
                        cyclesSense = Integer.valueOf(ap.getOption("cycles_sense"));
                    }
                    if (ap.getOption("cycles_deliberate") != null) {
                        cyclesDeliberate = Integer.valueOf(ap.getOption("cycles_deliberate"));
                    }
                    if (ap.getOption("cycles_act") != null) {
                        cyclesAct = Integer.valueOf(ap.getOption("cycles_act"));
                    }

                    // Create agents according to the specific architecture
                    LocalAgArch agArch;
                    if (agentConf == RConf.POOL_SYNCH) {
                        agArch = new LocalAgArchForPool();
                    } else if (agentConf == RConf.POOL_SYNCH_SCHEDULED) {
                        agArch = new LocalAgArchSynchronousScheduled();
                    } else if  (agentConf == RConf.ASYNCH || agentConf == RConf.ASYNCH_SHARED_POOLS) {
                        agArch = new LocalAgArchAsynchronous();
                    } else {
                        agArch = new LocalAgArch();
                    }

                    agArch.setCycles(cycles);
                    agArch.setCyclesSense(cyclesSense);
                    agArch.setCyclesDeliberate(cyclesDeliberate);
                    agArch.setCyclesAct(cyclesAct);

                    agArch.setConf(agentConf);
                    agArch.setAgName(numberedAg);
                    agArch.setEnvInfraTier(env);
                    if ((generalConf != RConf.THREADED) && cAg > 0 && ap.getAgArchClasses().isEmpty() && ap.getBBClass().getClassName().equals(DefaultBeliefBase.class.getName())) {
                        // creation by cloning previous agent (which is faster -- no parsing, for instance)
                        agArch.createArchs(ap.getAgArchClasses(), pag);
                    } else {
                        // normal creation
                        agArch.createArchs(ap.getAgArchClasses(), ap.agClass.getClassName(), ap.getBBClass(), ap.getSource().toString(), ap.getAsSetts(debug, project.getControlClass() != null));
                    }
                    addAg(agArch);
                    createdAgents.add(agArch); // used latter to start

                    pag = agArch.getTS().getAg();
                }
            } catch (jason.asSyntax.parser.ParseException e) {
                logger.log(Level.SEVERE,"as2j: error parsing \"" + ap.getSource() + "\": "+e.getMessage());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating agent " + ap.name, e);
            }
        }

        if (generalConf != RConf.THREADED) logger.info("Created "+nbAg+" agents.");
    }



    protected void createController() throws JasonException {
        ClassParameters controlClass = project.getControlClass();
        if (debug && controlClass == null) {
            controlClass = new ClassParameters(ExecutionControlGUI.class.getName());
        }
        if (controlClass != null) {
            logger.fine("Creating controller " + controlClass);
            control = new LocalExecutionControl(controlClass, this);
        }
    }

    protected void startAgs() {
        // run the agents
        if (project.getInfrastructure().hasParameter("pool") || project.getInfrastructure().hasParameter("synch_scheduled") || project.getInfrastructure().hasParameter("asynch") || project.getInfrastructure().hasParameter("asynch_shared")) {
            createThreadPool();
        } else {
            createAgsThreads();
        }
    }

    /** creates one thread per agent */
    private void createAgsThreads() {

        int cyclesSense      = 1;
        int cyclesDeliberate = 1;
        int cyclesAct        = 1;

        if (project.getInfrastructure().hasParameters()) {
            if (project.getInfrastructure().getParametersArray().length > 2) {
                cyclesSense      = Integer.parseInt(project.getInfrastructure().getParameter(1));
                cyclesDeliberate = Integer.parseInt(project.getInfrastructure().getParameter(2));
                cyclesAct        = Integer.parseInt(project.getInfrastructure().getParameter(3));
            } else if (project.getInfrastructure().getParametersArray().length > 1) {
                cyclesSense = cyclesDeliberate = cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(1));
            }

            //logger.info("Creating a threaded agents." + "Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct);
        }

        for (LocalAgArch ag : createdAgents) { //  ags.values()) { <<< removed, since agent can be created meanwhile and re-started here
            ag.setControlInfraTier(control);

            // if the agent hasn't override the values for cycles, use the platform values
            if (ag.getCyclesSense() == -1)            ag.setCyclesSense(cyclesSense);
            if (ag.getCyclesDeliberate() == -1)       ag.setCyclesDeliberate(cyclesDeliberate);
            if (ag.getCyclesAct() == -1)              ag.setCyclesAct(cyclesAct);

            // create the agent thread
            if (ag.getThread() == null)
                ag.setThread(new Thread(ag));
        }

        //logger.info("Creating threaded agents. Cycles: " + agTemp.getCyclesSense() + ", " + agTemp.getCyclesDeliberate() + ", " + agTemp.getCyclesAct());

        for (LocalAgArch ag : createdAgents) {
            ag.startThread();
        }
    }

    private Set<LocalAgArch> sleepingAgs;

    private ExecutorService executor            = null;

    private ExecutorService executorSense       = null;
    private ExecutorService executorDeliberate  = null;
    private ExecutorService executorAct         = null;

    /** creates a pool of threads shared by all agents */
    private void createThreadPool() {
        sleepingAgs = Collections.synchronizedSet(new HashSet<LocalAgArch>());

        int maxthreads = 10;

        int maxthreadsSense      = 1;
        int maxthreadsDeliberate = 1;
        int maxthreadsAct        = 1;

        int cycles           = 1;
        int cyclesSense      = 1;
        int cyclesDeliberate = 1;
        int cyclesAct        = 1;

        try {
            ClassParameters infra = project.getInfrastructure();
            RConf conf = RConf.fromString(infra.getParameter(0));
            if (conf == RConf.ASYNCH) {
                maxthreadsSense      = Integer.parseInt(infra.getParameter(1));
                maxthreadsDeliberate = Integer.parseInt(infra.getParameter(2));
                maxthreadsAct        = Integer.parseInt(infra.getParameter(3));
                if (infra.getParametersArray().length > 5) {
                    cyclesSense      = Integer.parseInt(infra.getParameter(4));
                    cyclesDeliberate = Integer.parseInt(infra.getParameter(5));
                    cyclesAct        = Integer.parseInt(infra.getParameter(6));
                }
                logger.info("Creating agents with asynchronous reasoning cycle. Sense (" + maxthreadsSense + "), Deliberate (" + maxthreadsDeliberate + "), Act (" + maxthreadsAct + "). Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct);

                executorSense      = Executors.newFixedThreadPool(maxthreadsSense);
                executorDeliberate = Executors.newFixedThreadPool(maxthreadsDeliberate);
                executorAct        = Executors.newFixedThreadPool(maxthreadsAct);

            } else { // async shared and pool cases
                if (infra.getParametersArray().length > 1) {
                    maxthreads = Integer.parseInt(infra.getParameter(1));
                }
                if (infra.getParametersArray().length > 4) {
                    cyclesSense      = Integer.parseInt(infra.getParameter(2));
                    cyclesDeliberate = Integer.parseInt(infra.getParameter(3));
                    cyclesAct        = Integer.parseInt(infra.getParameter(4));
                }

                if (conf == RConf.ASYNCH_SHARED_POOLS) {
                    logger.info("Creating agents with asynchronous reasoning cycle (shared). Sense, Deliberate, Act (" + maxthreads + "). Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct);
                    executorSense = executorDeliberate = executorAct = Executors.newFixedThreadPool(maxthreads);

                } else { // pool c  ases
                    if (conf == RConf.POOL_SYNCH) {
                        // redefine cycles
                        if (infra.getParametersArray().length == 3) {
                            cycles = Integer.parseInt(infra.getParameter(2));
                        } else  if (infra.getParametersArray().length == 6) {
                            cycles = Integer.parseInt(infra.getParameter(5));
                        } else {
                            cycles = 5;
                        }
                    } else if (infra.getParametersArray().length == 3) {
                        cyclesSense = cyclesDeliberate = cyclesAct = Integer.parseInt(infra.getParameter(2));
                    }

                    int poolSize = Math.min(maxthreads, ags.size());
                    logger.info("Creating a thread pool with "+poolSize+" thread(s). Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct + ". Reasoning Cycles: " + cycles);

                    // create the pool
                    executor = Executors.newFixedThreadPool(poolSize);
                }
            }
        } catch (Exception e) {
            logger.warning("Error getting the number of thread for the pool.");
        }

        // initially, add all agents in the tasks
        for (LocalAgArch ag : createdAgents) { //  ags.values()) { <<< removed, since agent can be created meanwhile and re-started here
            if (ag.getCycles() == -1)           ag.setCycles(cycles);
            if (ag.getCyclesSense() == -1)      ag.setCyclesSense(cyclesSense);
            if (ag.getCyclesDeliberate() == -1) ag.setCyclesDeliberate(cyclesDeliberate);
            if (ag.getCyclesAct() == -1)        ag.setCyclesAct(cyclesAct);

            if (executor != null) {
                if (ag instanceof LocalAgArchForPool)
                    ((LocalAgArchForPool)ag).setExecutor(executor);
                executor.execute(ag);
            } else if (ag instanceof LocalAgArchAsynchronous) {
                LocalAgArchAsynchronous ag2 = (LocalAgArchAsynchronous) ag;

                ag2.addListenerToC(new CircumstanceListenerComponents(ag2));

                ag2.setExecutorAct(executorAct);
                executorAct.execute(ag2.getActComponent());

                ag2.setExecutorDeliberate(executorDeliberate);
                executorDeliberate.execute(ag2.getDeliberateComponent());

                ag2.setExecutorSense(executorSense);
                executorSense.execute(ag2.getSenseComponent());
            }
        }
    }

    /** an agent architecture for the infra based on thread pool */
    protected final class LocalAgArchSynchronousScheduled extends LocalAgArch {
        private static final long serialVersionUID = 2752327732263465482L;

        private volatile boolean runWakeAfterTS = false;
        private int currentStep = 0;

        @Override
        public void sleep() {
            sleepingAgs.add(this);
        }

        @Override
        public void wake() {
            if (sleepingAgs.remove(this)) {
                executor.execute(this);
            } else {
                runWakeAfterTS = true;
            }
        }

        public void sense() {
            int number_cycles = getCyclesSense();
            int i = 0;

            while (isRunning() && i < number_cycles) {
                runWakeAfterTS = false;
                getTS().sense();
                if (getTS().canSleepSense()) {
                    if (runWakeAfterTS) {
                        wake();
                    }
                    break;
                }
                i++;
            }

            if (isRunning()) {
                executor.execute(this);
            }
        }

        public void deliberate() {
            super.deliberate();

            if (isRunning()) {
                executor.execute(this);
            }
        }

        public void act() {
            super.act();

            if (isRunning()) {
                executor.execute(this);
            }
        }

        @Override
        public void run() {
            switch (currentStep) {
            case 0:
                sense();
                currentStep = 1;
                break;
            case 1:
                deliberate();
                currentStep = 2;
                break;
            case 2:
                act();
                currentStep = 0;
                break;
            }
        }
    }

    protected void stopAgs(int deadline) {
        // if deadline is not 0, give agents some time
        if (deadline != 0) {
            for (AgArch ag: ags.values()) {
                Trigger te = PlanLibrary.TE_JAG_SHUTTING_DOWN.clone();
                te.getLiteral().addTerm(new NumberTermImpl(deadline));
                ag.getTS().getC().addExternalEv(te);
            }
            try {
                Thread.sleep(deadline);
            } catch (InterruptedException e) {}
        }

        // stop the agents
        for (LocalAgArch ag : new ArrayList<>(ags.values())) {
            try {
                ag.stopAg();
            } catch (Throwable e) {
                // ignore, the stop of agent should handled that
                // here, just keep stopping the system
            }
            delAg(ag.getAgName());
        }
    }

    public boolean killAg(String agName) {
        return RuntimeServicesFactory.get().killAgent(agName, "??", 0);
    }

    /** change the current running MAS to debug mode */
    protected void changeToDebugMode() {
        try {
            if (control == null) {
                control = new LocalExecutionControl(new ClassParameters(ExecutionControlGUI.class.getName()), this);
                for (LocalAgArch ag : ags.values()) {
                    ag.setControlInfraTier(control);
                    Settings stts = ag.getTS().getSettings();
                    stts.setVerbose(2);
                    stts.setSync(true);
                    ag.getLogger().setLevel(Level.FINE);
                    ag.getTS().getLogger().setLevel(Level.FINE);
                    ag.getTS().getAg().getLogger().setLevel(Level.FINE);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error entering in debug mode", e);
        }
    }


    protected void startSyncMode() {
        if (control != null) {
            // start the execution, if it is controlled
            try {
                Thread.sleep(500); // gives a time to agents enter in wait
                control.informAllAgsToPerformCycle(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void waitEnd() {
        try {
            // wait a file called .stop___MAS to be created!
            File stop = new File(stopMASFileName);
            if (stop.exists()) {
                stop.delete();
            }
            while (!stop.exists()) {
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected AtomicBoolean isRunningFinish = new AtomicBoolean(false);

    public void finish(int deadline, boolean stopJVM, int exitValue) {
        // avoid two threads running finish!
        if (isRunningFinish.getAndSet(true))
            return;

        isRunning = false;
        try {
            // creates a thread that guarantees system.exit(.) in deadline seconds
            // (the stop of agents can block, for instance)
            if (stopJVM) {
                new Thread() {
                    public void run() {
                        try {
                            if (deadline == 0)
                                sleep(5000);
                            else
                                sleep(deadline);
                        } catch (InterruptedException e) {}
                        System.exit(exitValue);
                    }
                } .start();
            }

            // use a thread to not block the caller
            new Thread() {
                public void run() {
                    System.out.flush();
                    System.err.flush();

                    if (MASConsoleGUI.hasConsole()) { // should close first! (case where console is in pause)
                        MASConsoleGUI.get().close();
                    }

                    stopAgs(deadline);

                    if (control != null) {
                        control.stop();
                        control = null;
                    }
                    if (env != null) {
                        env.stop();
                        env = null;
                    }

                    // remove the .stop___MAS file  (note that GUI console.close(), above, creates this file)
                    File stop = new File(stopMASFileName);
                    if (stop.exists()) {
                        stop.delete();
                    }

                    try {
                        ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName("jason.sf.net:type=runner"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (stopJVM) {
                        System.exit(exitValue);
                    }
                    isRunningFinish.set(false);
                };
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** show the sources of the project */
    private static void showProjectSources(MAS2JProject project) {
        JFrame frame = new JFrame("Project "+project.getSocName()+" sources");
        JTabbedPane pane = new JTabbedPane();
        frame.getContentPane().add(pane);
        project.fixAgentsSrc();

        for (AgentParameters ap : project.getAgents()) {
            try {
                String tmpAsSrc = ap.getSource().toString();

                // read sources
                InputStream in = null;
                if (tmpAsSrc.startsWith(SourcePath.CRPrefix)) {
                    in = RunLocalMAS.class.getResource(tmpAsSrc.substring(SourcePath.CRPrefix.length())).openStream();
                } else {
                    try {
                        in = new URL(tmpAsSrc).openStream();
                    } catch (MalformedURLException e) {
                        in = new FileInputStream(tmpAsSrc);
                    }
                }
                StringBuilder s = new StringBuilder();
                int c = in.read();
                while (c > 0) {
                    s.append((char)c);
                    c = in.read();
                }

                // show sources
                JTextArea ta = new JTextArea(40,50);
                ta.setEditable(false);
                ta.setText(s.toString());
                ta.setCaretPosition(0);
                JScrollPane sp = new JScrollPane(ta);
                pane.add(ap.name, sp);
            } catch (Exception e) {
                logger.info("Error:"+e);
            }
        }
        frame.pack();
        frame.setVisible(true);
    }


}
