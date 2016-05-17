//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini and Jomi F. Hubner
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.infra.centralised;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jason.JasonException;
import jason.asSemantics.Agent;
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
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;
import jason.util.Config;

/**
 * Runs MASProject using centralised infrastructure.
 */
public class RunCentralisedMAS {

    public final static String       logPropFile     = "logging.properties";
    public final static String       stopMASFileName = ".stop___MAS";
    public final static String       defaultProjectFileName = "default.mas2j";

    protected static Logger            logger        = Logger.getLogger(RunCentralisedMAS.class.getName());
    protected static RunCentralisedMAS runner        = null;
    protected static String            urlPrefix     = "";
    protected static boolean           readFromJAR   = false;
    protected static MAS2JProject      project;
    protected static boolean           debug         = false;
    
    protected CentralisedEnvironment        env         = null;
    protected CentralisedExecutionControl   control     = null;
    protected Map<String,CentralisedAgArch> ags         = new ConcurrentHashMap<String,CentralisedAgArch>();

    public JButton                   btDebug;
    
    public enum RConf {
        TRHEADED,
        POOL_SYNCH,
        POOL_SYNCH_SCHEDULED,
        ASYNCH,
        ASYNCH_SHARED_POOLS
    }
    
    public RunCentralisedMAS() {
        runner = this;  
    }
    
    public static void main(String[] args) throws JasonException {
        runner = new RunCentralisedMAS();
        runner.init(args);
        runner.create();
        runner.start();
        runner.waitEnd();
        runner.finish();
    }
        
    public int init(String[] args) {
        String projectFileName = null;
        if (args.length < 1) {
            if (RunCentralisedMAS.class.getResource("/"+defaultProjectFileName) != null) {
                projectFileName = defaultProjectFileName;
                readFromJAR = true;
                Config.get(false); // to void to call fix/store the configuration in this case everything is read from a jar/jnlp file
            } else {
                System.out.println("Jason "+Config.get().getJasonRunningVersion());
                System.err.println("You should inform the MAS project file.");
                JOptionPane.showMessageDialog(null,"You should inform the project file as a parameter.\n\nJason version "+Config.get().getJasonRunningVersion()+" library built on "+Config.get().getJasonBuiltDate(),"Jason", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        } else {
            projectFileName = args[0];
        }

        setupLogger();

        if (args.length >= 2) {
            if (args[1].equals("-debug")) {
                debug = true;
                Logger.getLogger("").setLevel(Level.FINE);
            }
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
            InputStream inProject;
            if (readFromJAR) {
                inProject = RunCentralisedMAS.class.getResource("/"+defaultProjectFileName).openStream();
                urlPrefix = Include.CRPrefix + "/";
            } else {
                URL file;
                // test if the argument is an URL
                try {
                    file = new URL(projectFileName);
                    if (projectFileName.startsWith("jar")) {
                        urlPrefix = projectFileName.substring(0,projectFileName.indexOf("!")+1) + "/";
                    }
                } catch (Exception e) {
                    file = new URL("file:"+projectFileName);
                }
                inProject = file.openStream();
            }
            jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(inProject); 
            project = parser.mas();
            project.setupDefault();

            project.registerDirectives();
            // set the aslSrcPath in the include
            ((Include)DirectiveProcessor.getDirective("include")).setSourcePath(project.getSourcePaths());
            
            project.fixAgentsSrc(urlPrefix);

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

    /** create environment, agents, controller */
    public void create() throws JasonException {
        createEnvironment();
        createAgs();
        createController();        
    }
    
    /** start agents, .... */
    public void start() {
        startAgs();
        startSyncMode();
    }
    
    public static boolean isDebug() {
        return debug;
    }

    public static synchronized void setupLogger() {
        if (readFromJAR) {
            Handler[] hs = Logger.getLogger("").getHandlers(); 
            for (int i = 0; i < hs.length; i++) { 
                Logger.getLogger("").removeHandler(hs[i]); 
            }
            Handler h = new MASConsoleLogHandler();
            h.setFormatter(new MASConsoleLogFormatter()); 
            Logger.getLogger("").addHandler(h);
            Logger.getLogger("").setLevel(Level.INFO);
        } else {
            // checks a local log configuration file
            if (new File(logPropFile).exists()) {
                try {
                    LogManager.getLogManager().readConfiguration(new FileInputStream(logPropFile));
                } catch (Exception e) {
                    System.err.println("Error setting up logger:" + e);
                }
            } else {
                try {
                    if (runner != null) {
                        LogManager.getLogManager().readConfiguration(runner.getDefaultLogProperties());
                    } else {
                        LogManager.getLogManager().readConfiguration(RunCentralisedMAS.class.getResource("/templates/" + logPropFile).openStream());                        
                    }
                } catch (Exception e) {
                    System.err.println("Error setting up logger:" + e);
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected InputStream getDefaultLogProperties() throws IOException {
        return RunCentralisedMAS.class.getResource("/templates/" + logPropFile).openStream();
    }
    
    public static void setupDefaultConsoleLogger() {
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
        runner.btDebug = new JButton("Debug", new ImageIcon(RunCentralisedMAS.class.getResource("/images/debug.gif")));
        runner.btDebug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                runner.changeToDebugMode();
                runner.btDebug.setEnabled(false);
                if (runner.control != null) {
                    try {
                        runner.control.getUserControl().setRunningCycle(false);
                    } catch (Exception e) { }
                }
            }
        });
        if (debug) {
            runner.btDebug.setEnabled(false);
        }
        MASConsoleGUI.get().addButton(runner.btDebug);

        // add show sources button
        final JButton btShowSrc = new JButton("Sources", new ImageIcon(RunCentralisedMAS.class.getResource("/images/list.gif")));
        btShowSrc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showProjectSources(project);
            }
        });
        MASConsoleGUI.get().addButton(btShowSrc);

        // add Button start
        final JButton btStartAg = new JButton("New agent", new ImageIcon(RunCentralisedMAS.class.getResource("/images/newAgent.gif")));
        btStartAg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new StartNewAgentGUI(MASConsoleGUI.get().getFrame(), "Start a new agent to run in current MAS", System.getProperty("user.dir"));
            }
        });
        MASConsoleGUI.get().addButton(btStartAg);

        // add Button kill
        final JButton btKillAg = new JButton("Kill agent", new ImageIcon(RunCentralisedMAS.class.getResource("/images/killAgent.gif")));
        btKillAg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new KillAgentGUI(MASConsoleGUI.get().getFrame(), "Kill an agent of the current MAS");
            }
        });
        MASConsoleGUI.get().addButton(btKillAg);
        
        createNewReplAgButton();
    }

    protected void createPauseButton() {
        final JButton btPause = new JButton("Pause", new ImageIcon(RunCentralisedMAS.class.getResource("/images/resume_co.gif")));
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
        JButton btStop = new JButton("Stop", new ImageIcon(RunCentralisedMAS.class.getResource("/images/suspend.gif")));
        btStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                MASConsoleGUI.get().setPause(false);
                runner.finish();
            }
        });
        MASConsoleGUI.get().addButton(btStop);
    }
    
    protected void createNewReplAgButton() {
        // add Button debug
        final JButton btStartAg = new JButton("New REPL agent", new ImageIcon(RunCentralisedMAS.class.getResource("/images/newAgent.gif")));
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
        CentralisedAgArch agArch = new CentralisedAgArch();
        agArch.setAgName(n);
        agArch.setEnvInfraTier(env);
        try {
            agArch.createArchs(null, ReplAgGUI.class.getName(), null, null, new Settings(), RunCentralisedMAS.this);
            Thread agThread = new Thread(agArch);
            agArch.setThread(agThread);
            agThread.start();                            
        } catch (JasonException e1) {
            e1.printStackTrace();
        }
        addAg(agArch);
    }

    
    public static RunCentralisedMAS getRunner() {
        return runner;
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(runner);
    }
    
    public CentralisedExecutionControl getControllerInfraTier() {
        return control;
    }

    public CentralisedEnvironment getEnvironmentInfraTier() {
        return env;
    }
    
    public MAS2JProject getProject() {
        return project;
    }
    public void setProject(MAS2JProject p) {
        project = p;
    }

    public void createEnvironment() throws JasonException {
        if (project.getEnvClass() != null && !project.getEnvClass().getClassName().equals(jason.environment.Environment.class.getName())) {
            logger.fine("Creating environment " + project.getEnvClass());
            env = new CentralisedEnvironment(project.getEnvClass(), this);
        }
    }

    
    public void createAgs() throws JasonException {
        
        RConf generalConf;
        if (project.getInfrastructure().hasParameter("pool")) {
            generalConf = RConf.POOL_SYNCH;
        } else if (project.getInfrastructure().hasParameter("synch_scheduled")) {
            generalConf = RConf.POOL_SYNCH_SCHEDULED;
        } else if (project.getInfrastructure().hasParameter("asynch")) {
            generalConf = RConf.ASYNCH;
        } else if (project.getInfrastructure().hasParameter("asynch_shared")) {
            generalConf = RConf.ASYNCH_SHARED_POOLS;
        } else {
            generalConf = RConf.TRHEADED;
        }
        
        //boolean isPool = project.getInfrastructure().hasParameter("pool") || project.getInfrastructure().hasParameter("synch_scheduled");
        //boolean isAsynch = project.getInfrastructure().hasParameter("asynch") || project.getInfrastructure().hasParameter("asynch_shared");
        if (generalConf != RConf.TRHEADED) logger.info("Creating agents....");
        int nbAg = 0;
        Agent pag = null;
        
        // create the agents
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
                    
                    String nb = "";
                    int    n  = 1;
                    while (getAg(numberedAg+nb) != null)
                        nb = "_" + (n++);
                    numberedAg += nb;
                    
                    logger.fine("Creating agent " + numberedAg + " (" + (cAg + 1) + "/" + ap.getNbInstances() + ")");
                    CentralisedAgArch agArch;
                    
                    RConf agentConf;
                    if (ap.getOption("rc") != null) {
                        if (ap.getOption("rc").equals("pool")) {
                            agentConf = RConf.POOL_SYNCH;
                        } else if (ap.getOption("rc").equals("synch_scheduled")) {
                            agentConf = RConf.POOL_SYNCH_SCHEDULED;
                        } else if (ap.getOption("rc").equals("asynch")) {
                            agentConf = RConf.ASYNCH;
                        } else if (ap.getOption("rc").equals("asynch_shared")) {
                            agentConf = RConf.ASYNCH_SHARED_POOLS;
                        } else {
                            agentConf = RConf.TRHEADED;
                        }
                    } else {
                        agentConf = generalConf;
                    }
                    
                    //Get the number of reasoning cycles or number of cycles for each stage 
                    int cycles = 0;
                    int cyclesSense = 0;
                    int cyclesDeliberate = 0; 
                    int cyclesAct = 0;
                    
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
                    
                    //Create agents according to the specific architecture
                    if (agentConf == RConf.POOL_SYNCH) {
                        agArch = new CentralisedAgArchForPool();
                    } else if (agentConf == RConf.POOL_SYNCH_SCHEDULED) {
                        agArch = new CentralisedAgArchSynchronousScheduled();
                        if (cycles != 0) {
                            if (cyclesSense == 0) {
                                cyclesSense = cycles;
                            }
                            if (cyclesDeliberate == 0) {
                                cyclesDeliberate = cycles;
                            }
                            if (cyclesAct == 0) {
                                cyclesAct = cycles;
                            }
                            cycles = 1;
                        }
                    } else if  (agentConf == RConf.ASYNCH || agentConf == RConf.ASYNCH_SHARED_POOLS) {
                        agArch = new CentralisedAgArchAsynchronous();
                        if (cycles != 0) {
                            if (cyclesSense == 0) {
                                cyclesSense = cycles;
                            }
                            if (cyclesDeliberate == 0) {
                                cyclesDeliberate = cycles;
                            }
                            if (cyclesAct == 0) {
                                cyclesAct = cycles;
                            }
                            cycles = 1;
                        }                       
                    } else {
                        agArch = new CentralisedAgArch();
                        if (cycles != 0) {
                            if (cyclesSense == 0) {
                                cyclesSense = cycles;
                            }
                            if (cyclesDeliberate == 0) {
                                cyclesDeliberate = cycles;
                            }
                            if (cyclesAct == 0) {
                                cyclesAct = cycles;
                            }
                            cycles = 1;
                        }
                    }
                    
                    agArch.setCycles(cycles);
                    agArch.setCyclesSense(cyclesSense);
                    agArch.setCyclesDeliberate(cyclesDeliberate);
                    agArch.setCyclesAct(cyclesAct);

                    agArch.setConf(agentConf);
                    agArch.setAgName(numberedAg);
                    agArch.setEnvInfraTier(env);
                    if ((generalConf != RConf.TRHEADED) && cAg > 0 && ap.getAgArchClasses().isEmpty() && ap.getBBClass().equals(DefaultBeliefBase.class.getName())) {
                        // creation by cloning previous agent (which is faster -- no parsing, for instance)
                        agArch.createArchs(ap.getAgArchClasses(), pag, this);
                    } else {
                        // normal creation
                        agArch.createArchs(ap.getAgArchClasses(), ap.agClass.getClassName(), ap.getBBClass(), ap.asSource.toString(), ap.getAsSetts(debug, project.getControlClass() != null), this);
                    }
                    addAg(agArch);
                    
                    pag = agArch.getTS().getAg();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating agent " + ap.name, e);
            }
        }
        
        if (generalConf != RConf.TRHEADED) logger.info("Created "+nbAg+" agents.");
    }
    
    /*
    public void createAgs() throws JasonException {
        boolean isPool = project.getInfrastructure().hasParameter("pool") || project.getInfrastructure().hasParameter("synch_scheduled");
        boolean isAsynch = project.getInfrastructure().hasParameter("asynch") || project.getInfrastructure().hasParameter("asynch_shared");
        if (isPool || isAsynch) logger.info("Creating agents....");
        int nbAg = 0;
        Agent pag = null;
        
        // create the agents
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
                    
                    String nb = "";
                    int    n  = 1;
                    while (getAg(numberedAg+nb) != null)
                        nb = "_" + (n++);
                    numberedAg += nb;
                    
                    logger.fine("Creating agent " + numberedAg + " (" + (cAg + 1) + "/" + ap.getNbInstances() + ")");
                    CentralisedAgArch agArch;
                    if (isPool) {
                        if (project.getInfrastructure().hasParameter("synch_scheduled")) {
                            agArch = new CentralisedAgArchSynchronousScheduled();
                            if (ap.getOption("cycles") != null) {
                                agArch.setCycles(Integer.valueOf(ap.getOption("cycles")));
                            }
                        } else {
                            agArch = new CentralisedAgArchForPool();
                            if (ap.getOption("cycles") != null) {
                                agArch.setCycles(Integer.valueOf(ap.getOption("cycles")));
                            }
                        }
                    } else if (isAsynch) {
                        agArch = new CentralisedAgArchAsynchronous();
                        
                        int cyclesSense = 0;
                        int cyclesDeliberate = 0; 
                        int cyclesAct = 0;
                        
                        if (ap.getOption("cycles") != null) {
                            cyclesSense = cyclesDeliberate = cyclesAct = Integer.valueOf(ap.getOption("cycles"));
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
                        
                        ((CentralisedAgArchAsynchronous) agArch).getSenseComponent().setCycles(cyclesSense);
                        ((CentralisedAgArchAsynchronous) agArch).getDeliberateComponent().setCycles(cyclesDeliberate);
                        ((CentralisedAgArchAsynchronous) agArch).getActComponent().setCycles(cyclesAct);
                        
                    } else {
                        agArch = new CentralisedAgArch();
                        if (ap.getOption("cycles") != null) {
                            agArch.setCycles(Integer.valueOf(ap.getOption("cycles")));
                        }
                    }
                    agArch.setAgName(numberedAg);
                    agArch.setEnvInfraTier(env);
                    if ((isPool || isAsynch) && cAg > 0 && ap.getAgArchClasses().isEmpty() && ap.getBBClass().equals(DefaultBeliefBase.class.getName())) {
                        // creation by cloning previous agent (which is faster -- no parsing, for instance)
                        agArch.createArchs(ap.getAgArchClasses(), pag, this);
                    } else {
                        // normal creation
                        agArch.createArchs(ap.getAgArchClasses(), ap.agClass.getClassName(), ap.getBBClass(), ap.asSource.toString(), ap.getAsSetts(debug, project.getControlClass() != null), this);
                    }
                    addAg(agArch);
                    
                    pag = agArch.getTS().getAg();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating agent " + ap.name, e);
            }
        }
        
        if (isPool || isAsynch) logger.info("Created "+nbAg+" agents.");
    }*/

    public void createController() throws JasonException {
        ClassParameters controlClass = project.getControlClass();
        if (debug && controlClass == null) {
            controlClass = new ClassParameters(ExecutionControlGUI.class.getName());
        }
        if (controlClass != null) {
            logger.fine("Creating controller " + controlClass);
            control = new CentralisedExecutionControl(controlClass, this);
        }        
    }
    
    public void addAg(CentralisedAgArch ag) {
        ags.put(ag.getAgName(), ag);
    }
    public CentralisedAgArch delAg(String agName) {
        return ags.remove(agName);
    }
    
    public CentralisedAgArch getAg(String agName) {
        return ags.get(agName);
    }
    
    public Map<String,CentralisedAgArch> getAgs() {
        return ags;
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
        
        int cyclesSense = 1;
        int cyclesDeliberate = 1;
        int cyclesAct = 5;
        
        if (project.getInfrastructure().hasParameters()) {
            if (project.getInfrastructure().getParametersArray().length > 2) {
                cyclesSense = Integer.parseInt(project.getInfrastructure().getParameter(1));
                cyclesDeliberate = Integer.parseInt(project.getInfrastructure().getParameter(2));
                cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(3));                    
            } else if (project.getInfrastructure().getParametersArray().length > 1) {
                cyclesSense = cyclesDeliberate = cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(1));
            }
            
            //logger.info("Creating a threaded agents." + "Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct);
        }
        
        for (CentralisedAgArch ag : ags.values()) {
            ag.setControlInfraTier(control);
            
            ag.setCyclesSense(cyclesSense);
            ag.setCyclesDeliberate(cyclesDeliberate);
            ag.setCyclesAct(cyclesAct);
            
            // create the agent thread
            Thread agThread = new Thread(ag);
            ag.setThread(agThread);
            //agThread.start();
        }
        
        //CentralisedAgArch agTemp = ags.values().iterator().next();
        //logger.info("Creating threaded agents. Cycles: " + agTemp.getCyclesSense() + ", " + agTemp.getCyclesDeliberate() + ", " + agTemp.getCyclesAct());
        
        for (CentralisedAgArch ag : ags.values()) {
            ag.startThread();
        }
    }
    
    private Set<CentralisedAgArch> sleepingAgs;
    
    private ExecutorService executor;
    
    private ExecutorService executorSense;
    private ExecutorService executorDeliberate;
    private ExecutorService executorAct;
    
    /** creates a pool of threads shared by all agents */
    private void createThreadPool() {
        sleepingAgs = Collections.synchronizedSet(new HashSet<CentralisedAgArch>());
        
        int configuration = 0;

        int maxthreads = 10;
        
        int maxthreadsSense = 1;
        int maxthreadsDeliberate = 1;
        int maxthreadsAct = 1;
        
        int cycles = 1;
        int cyclesSense = 1;
        int cyclesDeliberate = 1;
        int cyclesAct = 5;
        
        try {
            if (project.getInfrastructure().hasParameters()) {
                
                if (project.getInfrastructure().hasParameter("asynch")) {
                    configuration = 1;
                    maxthreadsSense = Integer.parseInt(project.getInfrastructure().getParameter(1));
                    maxthreadsDeliberate = Integer.parseInt(project.getInfrastructure().getParameter(2));
                    maxthreadsAct = Integer.parseInt(project.getInfrastructure().getParameter(3));
                    if (project.getInfrastructure().getParametersArray().length > 5) {
                        cyclesSense = Integer.parseInt(project.getInfrastructure().getParameter(4));
                        cyclesDeliberate = Integer.parseInt(project.getInfrastructure().getParameter(5));
                        cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(6));
                    } else if (project.getInfrastructure().getParametersArray().length > 4) {
                        cyclesSense = cyclesDeliberate = cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(4));
                    }
                    logger.info("Creating agents with asynchronous reasoning cycle. Sense (" + maxthreadsSense + "), Deliberate (" + maxthreadsDeliberate + "), Act (" + maxthreadsAct + ")" + "Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct);
                } else if (project.getInfrastructure().hasParameter("asynch_shared")) {
                    configuration = 2;
                    maxthreads = Integer.parseInt(project.getInfrastructure().getParameter(1));
                    if (project.getInfrastructure().getParametersArray().length > 3) {
                        cyclesSense = Integer.parseInt(project.getInfrastructure().getParameter(2));
                        cyclesDeliberate = Integer.parseInt(project.getInfrastructure().getParameter(3));
                        cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(4));
                    } else if (project.getInfrastructure().getParametersArray().length > 2) {
                        cyclesSense = cyclesDeliberate = cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(2));
                    }
                    logger.info("Creating agents with asynchronous reasoning cycle (shared). Sense, Deliberate, Act (" + maxthreads + ")"  + "Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct);
                } else if (project.getInfrastructure().hasParameter("synch_scheduled")) {
                    maxthreads = Integer.parseInt(project.getInfrastructure().getParameter(1));
                    
                    if (project.getInfrastructure().getParametersArray().length > 3) {
                        cyclesSense = Integer.parseInt(project.getInfrastructure().getParameter(2));
                        cyclesDeliberate = Integer.parseInt(project.getInfrastructure().getParameter(3));
                        cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(4));                    
                    } else if (project.getInfrastructure().getParametersArray().length > 2) {
                        cyclesSense = cyclesDeliberate = cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(2));
                    }
                    
                    logger.info("Creating a thread pool with "+maxthreads+" thread(s)." + "Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct + " Reasoning Cycles: " + cycles);                  
                } else {
                    maxthreads = Integer.parseInt(project.getInfrastructure().getParameter(1));
                    
                    if (project.getInfrastructure().getParametersArray().length > 3) {
                        cyclesSense = Integer.parseInt(project.getInfrastructure().getParameter(2));
                        cyclesDeliberate = Integer.parseInt(project.getInfrastructure().getParameter(3));
                        cyclesAct = Integer.parseInt(project.getInfrastructure().getParameter(4));
                        if (project.getInfrastructure().getParametersArray().length > 4) {
                            cycles = Integer.parseInt(project.getInfrastructure().getParameter(5));
                        } else {
                            cycles = 5;                         
                        }                           
                    } else if (project.getInfrastructure().getParametersArray().length > 2) {
                        cycles = Integer.parseInt(project.getInfrastructure().getParameter(2));
                    } else {
                        cycles = 5;
                    }
                    
                    logger.info("Creating a thread pool with "+maxthreads+" thread(s)." + "Cycles: " + cyclesSense + ", " + cyclesDeliberate + ", " + cyclesAct + " Reasoning Cycles: " + cycles);
                }
            }
        } catch (Exception e) {
            logger.warning("Error getting the number of thread for the pool.");
        }

        if (configuration == 0) {
            int poolSize = Math.min(8, ags.size());
            if (poolSize > maxthreads) {
                poolSize = maxthreads;
            }
            
            // create the pool
            executor = Executors.newFixedThreadPool(poolSize);

            // initially, add all agents in the tasks
            for (CentralisedAgArch ag : ags.values()) {
                ag.setCycles(cycles);
                ag.setCyclesSense(cyclesSense);
                ag.setCyclesDeliberate(cyclesDeliberate);
                ag.setCyclesAct(cyclesAct);
                if (ag instanceof CentralisedAgArchForPool)
                    ((CentralisedAgArchForPool)ag).setExecutor(executor);
                executor.execute(ag);
            }
            
        } else {
            
            //TODO shared thread pool among the stages
            if (configuration == 2) {
                executorSense = executorDeliberate = executorAct = Executors.newFixedThreadPool(maxthreads);
            } else {
                executorSense = Executors.newFixedThreadPool(maxthreadsSense);
                executorDeliberate = Executors.newFixedThreadPool(maxthreadsDeliberate);
                executorAct = Executors.newFixedThreadPool(maxthreadsAct);
            }
            
            //ThreadMonitorAgents mon = new ThreadMonitorAgents();
            
            // initially, add all agents in the tasks
            for (CentralisedAgArch ag : ags.values()) {
                CentralisedAgArchAsynchronous ag2 = (CentralisedAgArchAsynchronous) ag;
                
                ag2.addListenerToC(new CircumstanceListenerComponents(ag2));
                
                ag2.setExecutorAct(executorAct);
                ag2.setCyclesAct(cyclesAct);
                executorAct.execute(ag2.getActComponent());
                
                ag2.setExecutorDeliberate(executorDeliberate);
                ag2.setCyclesDeliberate(cyclesDeliberate);
                executorDeliberate.execute(ag2.getDeliberateComponent());
                
                ag2.setExecutorSense(executorSense);
                ag2.setCyclesSense(cyclesSense);
                executorSense.execute(ag2.getSenseComponent());
                
                //mon.lst.add(ag2);
            }
            
            //new Thread(mon).start();
        }
    }
    
    /** an agent architecture for the infra based on thread pool */
    protected final class CentralisedAgArchSynchronousScheduled extends CentralisedAgArch {
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
    
    protected void stopAgs() {
        // stop the agents
        for (CentralisedAgArch ag : ags.values()) {
            ag.stopAg();
        }
    }

    /** change the current running MAS to debug mode */
    void changeToDebugMode() {
        try {
            if (control == null) {
                control = new CentralisedExecutionControl(new ClassParameters(ExecutionControlGUI.class.getName()), this);
                for (CentralisedAgArch ag : ags.values()) {
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

    public void waitEnd() {
        try {
            // wait a file called .stop___MAS to be created!
            File stop = new File(stopMASFileName);
            if (stop.exists()) {
                stop.delete();
            }
            while (!stop.exists()) {
                Thread.sleep(1500);
                /*
                boolean allSleep = true;
                for (CentralisedAgArch ag : ags.values()) {
                    //System.out.println(ag.getAgName()+"="+ag.canSleep());
                    allSleep = allSleep && ag.canSleep();
                }
                if (allSleep)
                    break;
                */
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Boolean runningFinish = false;
    public void finish() { 
        // avoid two threads running finish!
        synchronized (runningFinish) {
            if (runningFinish)
                return;
            runningFinish = true;            
        }
        try {
            // creates a thread that guarantees system.exit(0) in 5 seconds
            // (the stop of agents can  block)
            new Thread() {
                public void run() {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {}
                    System.exit(0);
                }
            }.start();
            
            System.out.flush();
            System.err.flush();

            if (MASConsoleGUI.hasConsole()) { // should close first! (case where console is in pause)
                MASConsoleGUI.get().close();
            }

            if (control != null) {
                control.stop();
                control = null;
            }
            if (env != null) {
                env.stop();
                env = null;
            }
            
            stopAgs();

            runner = null;
            
            // remove the .stop___MAS file  (note that GUI console.close(), above, creates this file)
            File stop = new File(stopMASFileName);
            if (stop.exists()) {
                stop.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
    
    /** show the sources of the project */
    private static void showProjectSources(MAS2JProject project) {
        JFrame frame = new JFrame("Project "+project.getSocName()+" sources");
        JTabbedPane pane = new JTabbedPane();
        frame.getContentPane().add(pane);
        project.fixAgentsSrc(urlPrefix);

        for (AgentParameters ap : project.getAgents()) {
            try {
                String tmpAsSrc = ap.asSource.toString();
                
                // read sources
                InputStream in = null;
                if (tmpAsSrc.startsWith(Include.CRPrefix)) {
                    in = RunCentralisedMAS.class.getResource(tmpAsSrc.substring(Include.CRPrefix.length())).openStream();
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
