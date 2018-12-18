package jason.jeditplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.gui.AnimatedIcon;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.RolloverButton;

import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;
import jason.architecture.MindInspectorWeb;
import jason.infra.MASLauncherInfraTier;
import jason.infra.RunProjectListener;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedMASLauncherAnt;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.TokenMgrError;
import jason.runtime.OutputStreamAdapter;
import jason.util.CheckVersion;
import jason.util.Config;
import jason.util.asl2html;
import jason.util.asl2tex;
import jason.util.asl2xml;

public class JasonID extends JPanel implements EBComponent, RunProjectListener {

    private static final long serialVersionUID = 1L;

    View                 view;
    OutputStreamAdapter  myOut;
    JTextArea            textArea;
    AnimatedIcon         animation;
    JButton              btStop;
    JButton              btRun;
    JButton              btDebug;
    DefaultListModel     listModel   = new DefaultListModel();
    JList                lstAgs      = new JList(listModel);

    DefaultErrorSource   errorSource = null;

    MASLauncherInfraTier masLauncher;

    public JasonID(View view, String position) {
        super(new BorderLayout());

        this.view = view;

        add(BorderLayout.NORTH, createToolBar());

        boolean floating = position.equals(DockableWindowManager.FLOATING);
        if (floating)
            this.setPreferredSize(new Dimension(500, 250));

        textArea = new JTextArea(5, 10);
        textArea.setEditable(false);
        textArea.setText("Further Jason functionalities are available in the menu Plugin->Jason.");

        JPanel pane = new JPanel(new BorderLayout());
        pane.add(BorderLayout.CENTER, new JScrollPane(textArea));
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Jason console", TitledBorder.LEFT, TitledBorder.TOP));
        add(BorderLayout.CENTER, pane);

        JPanel pLst = new JPanel(new BorderLayout());
        pLst.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Project agents", TitledBorder.LEFT, TitledBorder.TOP));

        pLst.add(BorderLayout.CENTER, new JScrollPane(lstAgs));
        // pLst.setMinimumSize(new Dimension(100,50));
        lstAgs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstAgs.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                openAgentBuffer((AgentParameters) lstAgs.getSelectedValue());
            }
        });
        pLst.setPreferredSize(new Dimension(120, 50));
        add(BorderLayout.EAST, pLst);

        myOut = new OutputStreamAdapter(null, textArea);
        myOut.setAsDefaultOut();

        // add myself in project parser
        JasonProjectSideKickParser.addPluginInstance(this);

        new CheckVersion().start();

        // stop mind inspector in case it was started
        new Thread() {
            public void run() {
                try {
                   sleep(4000);
                   MindInspectorWeb.stop();
                } catch (Exception e) {}
            };
        }.start();
    }

    public synchronized void start() {
    }

    public synchronized void stop() {
        stopMAS();
    }

    private JPanel createToolBar() {
        // JToolBar toolBar = new JToolBar();
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
        // toolBar.setFloatable(false);

        JLabel animationLabel = new JLabel();
        animationLabel.setBorder(new EmptyBorder(2, 3, 2, 3));
        Toolkit toolkit = getToolkit();
        animation = new AnimatedIcon(toolkit.getImage(JasonID.class.getResource("/images/Blank.png")), new Image[] {
                                         toolkit.getImage(JasonID.class.getResource("/images/Active1.png")), toolkit.getImage(JasonID.class.getResource("/images/Active2.png")),
                                         toolkit.getImage(JasonID.class.getResource("/images/Active3.png")), toolkit.getImage(JasonID.class.getResource("/images/Active4.png"))
                                     }, 10, animationLabel);
        animationLabel.setIcon(animation);
        toolBar.add(animationLabel);

        btRun = createToolBarButton("Run MAS", new ImageIcon(JasonID.class.getResource("/images/run.gif")), new ActionListener() { // GUIUtilities.loadIcon("Play.png")
            public void actionPerformed(ActionEvent arg0) {
                runProject();
            }
        });
        toolBar.add(btRun);

        btDebug = createToolBarButton("Debug MAS", new ImageIcon(JasonID.class.getResource("/images/debug.gif")), // GUIUtilities.loadIcon("RunAgain.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                debugProject();
            }
        });
        toolBar.add(btDebug);

        btStop = createToolBarButton("Stop MAS", new ImageIcon(JasonID.class.getResource("/images/suspend.gif")), // GUIUtilities.loadIcon("Stop.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                stopMAS();
            }
        });
        btStop.setEnabled(false);
        toolBar.add(btStop);

        toolBar.add(new JLabel(" | "));

        toolBar.add(createToolBarButton("Open Project", new ImageIcon(JasonID.class.getResource("/images/openProject.gif")), // GUIUtilities.loadIcon("NewDir.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                VFSFileChooserDialog dialog = new VFSFileChooserDialog(view, ".", VFSBrowser.OPEN_DIALOG, false);
                if (dialog.getSelectedFiles() != null && dialog.getSelectedFiles().length > 0) {
                    org.gjt.sp.jedit.jEdit.openFile(view, dialog.getSelectedFiles()[0].toString());
                }
            }
        }));
        toolBar.add(createToolBarButton("New Project", new ImageIcon(JasonID.class.getResource("/images/newProject.gif")), // GUIUtilities.loadIcon("NewDir.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newProject();
            }
        }));

        toolBar.add(createToolBarButton("Add agent in project", new ImageIcon(JasonID.class.getResource("/images/newAgent.gif")), // GUIUtilities.loadIcon("NextFile.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newAg();
            }
        }));

        toolBar.add(createToolBarButton("Create Environment", new ImageIcon(JasonID.class.getResource("/images/createEnv.gif")), // GUIUtilities.loadIcon("NextFile.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createEnv();
            }
        }));

        toolBar.add(createToolBarButton("Create Internal Action", new ImageIcon(JasonID.class.getResource("/images/createIA.gif")), // GUIUtilities.loadIcon("NextFile.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newIA();
            }
        }));

        toolBar.add(new JLabel(" | "));
        toolBar.add(createToolBarButton("Clear panel", new ImageIcon(JasonID.class.getResource("/images/clear.gif")),// GUIUtilities.loadIcon("Clear.png"),
        new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                textArea.setText("");
            }
        }));
        add(Box.createGlue());

        JPanel p = new JPanel(new BorderLayout());
        p.add(toolBar, BorderLayout.EAST);

        JButton about = new JButton("<html><center>about<br><i><b>Jason</b></i></center></html>", new ImageIcon(JasonID.class.getResource("/images/Jason-GMoreau-Small-Icon.jpg")));
        about.setFont(new Font("Arial", Font.PLAIN, 14));
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                AboutGUI.show(view);
            }
        });
        /*
        JLabel jasonLabel = new JLabel("Jason IDE");
        jasonLabel.setFont(new Font("Times", Font.BOLD | Font.ITALIC, 16));
        */
        JPanel pAbt = new JPanel();
        pAbt.add(about);
        //pAbt.add(jasonLabel);
        p.add(pAbt, BorderLayout.WEST);

        return p;
    }

    protected JButton createToolBarButton(String toolTip, Icon icon, ActionListener act) {
        JButton button = new RolloverButton(icon);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setRequestFocusEnabled(false);
        button.setToolTipText(toolTip);
        button.setRequestFocusEnabled(false);
        button.addActionListener(act);
        return button;
    }

    void openAgentBuffer(AgentParameters ap) {
        try {
            if (ap == null || ap.asSource == null)
                return;

            // fix file name
            Buffer projectb = getProjectBuffer();
            String file = ap.asSource.toString();
            if (file.indexOf('/') < 0 && file.indexOf('\\') < 0)
                // append project directory
                file = projectb.getDirectory() + "/" + file;
            file = new File(file).getAbsolutePath();

            boolean newFile = !(new File(file).exists());
            Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, file);
            // textArea.append(lstAgs.getSelectedValue()+"");
            if (newFile) {
                try {
                    ap.asSource = new File(projectb.getDirectory() + ap.name + "." + MAS2JProject.AS_EXT);
                    String agcode = Config.get().getTemplate("agent");
                    agcode = agcode.replace("<AG_NAME>", ap.getAgName());
                    agcode = agcode.replace("<PROJECT_NAME>", projectb.getName());
                    nb.writeLock();
                    nb.insert(0, agcode);
                    nb.save(view, file);
                } finally {
                    nb.writeUnlock();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void handleMessage(EBMessage message) {
        if (message == null)
            return;

        /*
         * if (message instanceof PropertiesChanged) { propertiesChanged(); }
         * else
         */

        /*
         * if (message instanceof BufferUpdate) { BufferUpdate bu =
         * (BufferUpdate)message;
         *
         * if ((bu.getWhat() == BufferUpdate.LOADED || bu.getWhat() ==
         * BufferUpdate.CREATED) &&
         * bu.getBuffer().getPath().endsWith(MAS2JProject.EXT)) {
         *
         * //String projName = bu.getBuffer().getName().substring(0,
         * bu.getBuffer().getName().length()-(MAS2JProject.EXT.length()+1));
         *
         * //checkProjectView(projName, new
         * File(bu.getBuffer().getDirectory()));
         *
         * //bu.getBuffer().setProperty("sidekick.parser",JasonSideKickParser.ID);
         *  } }
         */
    }

    public void addNotify() {
        super.addNotify();
        EditBus.addToBus(this);
    }

    public void removeNotify() {
        super.removeNotify();
        EditBus.removeFromBus(this);
    }

    /** returns the current MAS2J project */
    private Buffer getProjectBuffer() {
        if (view.getBuffer().getPath().endsWith(MAS2JProject.EXT)) {
            return view.getBuffer();
        }
        Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
        for (int i = 0; i < bufs.length; i++) {
            if (bufs[i].getPath().endsWith(MAS2JProject.EXT)) {
                return bufs[i];
            }
        }
        return null;
    }

    Buffer getProjectBuffer(String name) {
        Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
        for (int i = 0; i < bufs.length; i++) {
            if (bufs[i].getPath().endsWith(name)) {
                return bufs[i];
            }
        }
        return null;
    }

    private void saveAll() {

        Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
        for (int i = 0; i < bufs.length; i++) {
            if (bufs[i].isDirty()) {
                bufs[i].save(view, null);
            }
        }
    }

    private MAS2JProject parseProject(Buffer projectBufffer) {
        // compile
        try {
            textArea.append("Parsing project file... ");

            String text;
            try {
                projectBufffer.readLock();
                text = projectBufffer.getText(0, projectBufffer.getLength());
            } finally {
                projectBufffer.readUnlock();
            }

            jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new StringReader(text));
            MAS2JProject project = parser.mas();
            project.setDirectory(projectBufffer.getDirectory());
            project.setProjectFile(new File(projectBufffer.getPath()));
            project.fixAgentsSrc();
            textArea.append(" parsed successfully!\n");
            return project;

        } catch (ParseException ex) {
            textArea.append("\nmas2j: syntactic errors found... \n" + ex + "\n");
            JasonProjectSideKickParser.addError(ex, errorSource, projectBufffer.getPath());
        } catch (TokenMgrError ex) {
            textArea.append("\nmas2j: lexical errors found... \n" + ex + "\n");
        } catch (Exception ex) {
            textArea.append("Error: " + ex);
            ex.printStackTrace();
        }
        return null;
    }

    protected boolean parseProjectAS(MAS2JProject project) {
        // compile
        File asFile = null;
        try {
            Iterator<File> iASfile = project.getAllASFiles().iterator();
            while (iASfile.hasNext()) {
                asFile = iASfile.next();
                try {
                    textArea.append("Parsing AgentSpeak file '" + asFile.getName() + "'...");
                    jason.asSyntax.parser.as2j parser = new jason.asSyntax.parser.as2j(new FileReader(asFile));
                    parser.agent(null);
                    textArea.append(" parsed successfully!\n");
                } catch (FileNotFoundException ex) {
                    textArea.append(" error: file '"+asFile+"' not found!");
                    return false;
                }
            }
            return true;

        } catch (jason.asSyntax.parser.ParseException ex) {
            textArea.append("\nas2j: syntactic errors found... \n" + ex + "\n");
            AgentSpeakSideKickParser.addError(ex, errorSource, asFile.getAbsolutePath());
            DockableWindowManager d = view.getDockableWindowManager();
            if (!d.isDockableWindowVisible("error-list")) {
                d.addDockableWindow("error-list");
            }

        } catch (TokenMgrError ex) {
            textArea.append("\nmas2j: lexical errors found... \n" + ex + "\n");
        } catch (Exception ex) {
            textArea.append("\nError: " + ex);
            ex.printStackTrace();
        }
        return false;
    }

    //
    // RunningMASListener methods
    //
    public void masFinished() {
        animation.stop();
        btStop.setEnabled(false);
        btRun.setEnabled(true);
        btDebug.setEnabled(true);
    }

    //
    // Menu actions
    //

    public void runProject(final boolean debug) {
        final Buffer b = getProjectBuffer();
        if (b == null) {
            textArea.setText("There is no Jason project opened!");
            return;
        }
        textArea.setText("Launching " + b.getName() + "\n");
        if (errorSource == null) {
            errorSource = new DefaultErrorSource("JasonIDE");
            ErrorSource.registerErrorSource(errorSource);
        }

        errorSource.clear();
        saveAll();

        new Thread() {
            public void run() {

                // wait for buffers io
                Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
                int max = 1;
                for (int i = 0; i < bufs.length && max < 20; i++) {
                    if (bufs[i].isPerformingIO()) {
                        i--;
                        max++;
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) { }
                    }
                }

                MAS2JProject project = parseProject(b);
                if (project == null) // || !parseProjectAS(project)) {
                    return;

                // launch the MAS
                animation.start();
                btStop.setEnabled(true);
                btRun.setEnabled(false);
                btDebug.setEnabled(false);
                try {
                    if (masLauncher != null) {
                        // stops old running mas
                        masLauncher.stopMAS();
                    }

                    // Check configuration
                    String jasonJar = Config.get().getJasonJar();
                    if (!Config.checkJar(jasonJar)) {
                        System.err.println("The path to the jason.jar file (" + jasonJar + ") was not correctly set: the MAS may not run. Go to menu Plugins->Plugins Options->Jason to configure the path.");
                        return;
                    }
                    String javaHome = Config.get().getJavaHome();
                    if (!Config.checkJavaHomePath(javaHome)) {
                        System.err.println("The Java JDK directory (" + javaHome + ") was not correctly set: the MAS may not run. Go to the Plugins->Options->Jason menu to configure the path.");
                        //return;
                    }
                    String antLib = Config.get().getAntLib();
                    if (!Config.checkAntLib(antLib)) {
                        System.err.println("The ant lib directory (" + antLib + ") was not correctly set: the MAS may not run. Go to the Plugins->Options->Jason menu to configure the path.");
                        return;
                    }

                    masLauncher = project.getInfrastructureFactory().createMASLauncher();
                    masLauncher.setProject(project);
                    masLauncher.setListener(JasonID.this);
                    if (masLauncher.writeScripts(debug, true)) {
                        new Thread(masLauncher, "MAS-Launcher").start();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } .start();
    }

    public void runProject() {
        runProject(false);
    }

    public void debugProject() {
        runProject(true);
    }

    public void stopMAS() {
        animation.stop();
        btStop.setEnabled(false);
        btRun.setEnabled(true);
        btDebug.setEnabled(true);
        if (masLauncher != null) {
            masLauncher.stopMAS();
            masLauncher = null;
        }
    }

    public void newProject() {
        new NewProjectGUI("New Jason Project", view, this);
    }

    public void newAg() {
        Buffer b = getProjectBuffer();
        if (b == null) {
            textArea.setText("There is no Jason project opened to add an agent to: create a new project first.");
        } else {
            new NewAgentGUI("Adding an agent to project " + b.getName(), b, view, b.getDirectory());
        }
    }

    public void createEnv() {
        Buffer b = getProjectBuffer();
        if (b == null) {
            textArea.setText("There is no Jason project opened: create a new project first.");
        } else {
            if (b.getText(0, b.getLength()).indexOf("environment") > 0) {
                textArea.setText("Your project already has an environment definition!");
            } else {
                new NewEnvironmentGUI("Creating an environment for project " + b.getName(), b, view);
            }
        }
    }

    public void newIA() {
        Buffer b = getProjectBuffer();
        if (b == null) {
            textArea.setText("There is no Jason project opened: create a new project first.");
        } else {
            new NewInternalActionGUI("Creating new internal action for project " + b.getName(), b, view);
        }
    }

    public void editLog() {
        Buffer curBuf = getProjectBuffer();
        if (curBuf == null) {
            textArea.setText("There is no Jason project opened: you cannot edit logging properties.");
            return;
        }
        try {
            File f = new File(curBuf.getDirectory() + File.separator + BaseCentralisedMAS.logPropFile);
            if (f.exists()) {
                org.gjt.sp.jedit.jEdit.openFile(view, f.getAbsolutePath());
            } else {
                String logText = Config.get().getTemplate(BaseCentralisedMAS.logPropFile);
                Buffer b = org.gjt.sp.jedit.jEdit.openFile(view, f.getAbsolutePath());
                try {
                    b.writeLock();
                    b.insert(0, logText);
                    b.save(view, f.getAbsolutePath());
                } finally {
                    b.writeUnlock();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void asl2html() {
        asl2txt(new asl2html(), ".html");
    }
    public void asl2tex() {
        asl2txt(new asl2tex(), ".tex");
    }

    // TODO: does not work, may be a problem with jEdit (check with new versions)
    private void asl2txt(asl2xml transformer, String ext) {
        Buffer buf = view.getBuffer();
        if (!buf.getPath().endsWith(MAS2JProject.AS_EXT)) {
            textArea.setText("\n** The current buffer is not an AgentSpeak source!");
            return;
        } else {
            textArea.setText("");
        }
        try {
            String aslcode;
            try {
                buf.readLock();
                aslcode = buf.getText(0,buf.getLength());
            } finally {
                buf.readUnlock();
            }

            String  htmlcode = transformer.transform(aslcode);
            Buffer b = org.gjt.sp.jedit.jEdit.openFile(view,buf.getPath()+ext);
            try {
                b.writeLock();
                b.insert(0, htmlcode);
                b.save(view, buf.getPath()+".html");
            } finally {
                b.writeUnlock();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runAntTask(String task) {
        final Buffer b = getProjectBuffer();
        if (b == null) {
            textArea.setText("There is no Jason project opened!");
            return;
        }
        MAS2JProject project = parseProject(b);
        if (project == null) // || !parseProjectAS(project)) {
            return;
        CentralisedMASLauncherAnt script = new CentralisedMASLauncherAnt(task);
        script.setProject(project);
        if (script.writeScripts(false, true)) {
            new Thread(script, "Ant-Task").start();
        }

    }
}
