package jason.util;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

public class ConfigGUI {

    protected JTextField jasonTF;
    protected JTextField javaTF;
    protected JTextField antTF;
    protected JTextArea infraTP;
    //JCheckBox  insideJIDECBox;
    protected JCheckBox  closeAllCBox;
    protected JCheckBox  checkVersionCBox;
    protected JCheckBox  warnSingVarsCBox;

    protected JTextField jadeJarTF;
    protected JTextField jadeArgsTF;
    protected JCheckBox  jadeSnifferCB;
    protected JCheckBox  jadeRmaCB;

    protected JCheckBox  shortUnnamedVarCB;

    protected static Config userProperties = Config.get();

    static {
        String currJasonVersion = userProperties.getJasonVersion();

        // check new version
        //File jasonConfFile = getUserConfFile();
        if (userProperties.getProperty("version") != null) {
            //userProperties.load(new FileInputStream(jasonConfFile));
            if (!userProperties.getProperty("version").equals(currJasonVersion) && !currJasonVersion.equals("?")) {
                // new version, set all values to default
                System.out.println("This is a new version of Jason, reseting configuration...");
                //userProperties.clear();
                userProperties.remove(Config.JADE_JAR);
                userProperties.remove(Config.JASON_JAR);
                userProperties.remove(Config.ANT_LIB);
                userProperties.remove(Config.CHECK_VERSION);
            }
        }

        userProperties.fix();
        userProperties.store();
    }

    public static void main(String[] args) {
        new ConfigGUI().run();
    }

    protected static ConfigGUI getNewInstance() {
        return new ConfigGUI();
    }

    public void run() {
        final ConfigGUI jid = getNewInstance();
        JFrame f = null;
        try {
            f = new JFrame(jid.getWindowTitle());
        } catch (Exception e) {
            // uses console
            userProperties.fix();
            userProperties.store();
            System.out.println("\nYou can edit the file "+userProperties.getUserConfFile()+" for your local configuration.");
            return;
        }

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel pBt = new JPanel(new FlowLayout());
        JButton bQuit = new JButton("Exit without saving");
        bQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        pBt.add(bQuit);
        JButton bSave = new JButton("Save configuration and Exit");
        bSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                jid.save();
                System.exit(0);
            }
        });
        pBt.add(bSave);

        JPanel p = new JPanel(new BorderLayout());
        p.add(BorderLayout.CENTER, jid.getJasonConfigPanel());
        p.add(BorderLayout.SOUTH, pBt);

        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
    }

    protected String getWindowTitle() {
        return "Jason Configuration -- "+userProperties.getProperty("version");
    }

    public JPanel getJasonConfigPanel() {
        JPanel pop = new JPanel();
        pop.setLayout(new BoxLayout(pop, BoxLayout.Y_AXIS));

        // jason home
        jasonTF = new JTextField(25);
        JPanel jasonHomePanel = new JPanel(new GridLayout(0,1));
        jasonHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                                 .createEtchedBorder(), "Jason", TitledBorder.LEFT, TitledBorder.TOP));
        JPanel jasonJarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jasonJarPanel.add(new JLabel("jason.jar location"));
        jasonJarPanel.add(jasonTF);
        jasonJarPanel.add(createBrowseButton("jason.jar", jasonTF));
        jasonHomePanel.add(jasonJarPanel);

        // jason check version
        JPanel checkVersionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //checkVersionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Jason options", TitledBorder.LEFT, TitledBorder.TOP));
        checkVersionCBox = new JCheckBox("Check for new Jason versions on startup", false);
        checkVersionPanel.add(checkVersionCBox);
        jasonHomePanel.add(checkVersionPanel);

        // warn sing vars
        JPanel wsvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        warnSingVarsCBox = new JCheckBox("Print out warnings about singleton variables in plans and rules", false);
        wsvPanel.add(warnSingVarsCBox);
        jasonHomePanel.add(wsvPanel);

        // unamed vars style
        JPanel unvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        shortUnnamedVarCB = new JCheckBox("Use short names for unamed variables (those starting with _)", false);
        unvPanel.add(shortUnnamedVarCB);
        jasonHomePanel.add(unvPanel);

        pop.add(jasonHomePanel);


        // java home
        JPanel javaHomePanel = new JPanel();
        javaHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                                .createEtchedBorder(), "Java Home", TitledBorder.LEFT, TitledBorder.TOP));
        javaHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        javaHomePanel.add(new JLabel("Directory"));
        javaTF = new JTextField(25);
        javaHomePanel.add(javaTF);
        JButton setJava = new JButton("Browse");
        setJava.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                    chooser.setDialogTitle("Select the Java JDK home directory");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        String javaHome = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
                        if (Config.checkJavaHomePath(javaHome)) {
                            javaTF.setText(javaHome);
                        } else {
                            JOptionPane.showMessageDialog(null, "The selected JDK home directory doesn't have the bin/javac file!");
                        }
                    }
                } catch (Exception e) {}
            }
        });
        javaHomePanel.add(setJava);
        pop.add(javaHomePanel);

        // ant lib home
        JPanel antHomePanel = new JPanel();
        antHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                               .createEtchedBorder(), "Ant libs", TitledBorder.LEFT, TitledBorder.TOP));
        antHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        antHomePanel.add(new JLabel("Directory"));
        antTF = new JTextField(25);
        antHomePanel.add(antTF);
        JButton setAnt = new JButton("Browse");
        setAnt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                    chooser.setDialogTitle("Select the directory with ant.jar and ant-launcher.jar files");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        String antLib = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
                        if (Config.checkAntLib(antLib)) {
                            antTF.setText(antLib);
                        } else {
                            JOptionPane.showMessageDialog(null, "The selected directory doesn't have the files ant.jar and ant-launcher.jar!");
                        }
                    }
                } catch (Exception e) {}
            }
        });
        antHomePanel.add(setAnt);
        pop.add(antHomePanel);

        // infras
        JPanel infraPanel = new JPanel();
        infraPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                             .createEtchedBorder(), "Available Insfrastructures", TitledBorder.LEFT, TitledBorder.TOP));
        infraPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        infraTP = new JTextArea(5,45);
        infraPanel.add(new JScrollPane(infraTP));
        pop.add(infraPanel);


        // jade home
        jadeJarTF  = new JTextField(25);
        jadeArgsTF = new JTextField(30);
        JPanel jadeHomePanel = new JPanel(new GridLayout(0,1));
        jadeHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                                .createEtchedBorder(), "JADE", TitledBorder.LEFT, TitledBorder.TOP));

        JPanel jadeJarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jadeJarPanel.add(new JLabel("jade.jar location"));
        jadeJarPanel.add(jadeJarTF);
        jadeJarPanel.add(createBrowseButton("jade.jar", jadeJarTF));
        jadeHomePanel.add(jadeJarPanel);
        JPanel jadeArgsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jadeArgsPanel.add(new JLabel("jade.Boot arguments"));
        jadeArgsPanel.add(jadeArgsTF);
        jadeHomePanel.add(jadeArgsPanel);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jadeRmaCB = new JCheckBox();
        jadeRmaCB.setToolTipText("Should the JADE management agent be run at startup?");
        p.add(jadeRmaCB);
        p.add(new JLabel("Start management agent             "));

        jadeSnifferCB = new JCheckBox();
        jadeSnifferCB.setToolTipText("Should the JADE sniffer agent be run at startup?");
        p.add(jadeSnifferCB);
        p.add(new JLabel("Start Sniffer"));
        jadeHomePanel.add(p);

        pop.add(jadeHomePanel);

        // shell command
        /*
        JPanel shellPanel = new JPanel();
        shellPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Shell command", TitledBorder.LEFT, TitledBorder.TOP));
        shellPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        shellTF = new JTextField(30);
        shellTF.setToolTipText("This command will be used to run the scripts that run the MAS.");
        shellPanel.add(shellTF);
        pop.add(shellPanel);
        */

        // run centralised inside jIDE
        /*
        JPanel insideJIDEPanel = new JPanel();
        insideJIDEPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Centralised MAS execution mode", TitledBorder.LEFT, TitledBorder.TOP));
        insideJIDEPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        insideJIDECBox = new JCheckBox("Run MAS as a JasonIDE internal thread instead of another process.");
        insideJIDEPanel.add(insideJIDECBox);
        pop.add(insideJIDEPanel);
        */

        // close all before opening mas project
        JPanel closeAllPanel = new JPanel();
        closeAllPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "jEdit options", TitledBorder.LEFT, TitledBorder.TOP));
        closeAllPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        closeAllCBox = new JCheckBox("Close all files before opening a new Jason Project.");
        closeAllPanel.add(closeAllCBox);
        pop.add(closeAllPanel);

        jadeJarTF.setText(userProperties.getJadeJar());
        jadeArgsTF.setText(userProperties.getJadeArgs());
        jasonTF.setText(userProperties.getJasonJar());
        javaTF.setText(userProperties.getJavaHome());
        antTF.setText(userProperties.getAntLib());
        //shellTF.setText(userProperties.getShellCommand());
        //insideJIDECBox.setSelected(userProperties.runAsInternalTread());
        closeAllCBox.setSelected(userProperties.getBoolean(Config.CLOSEALL));
        checkVersionCBox.setSelected(userProperties.getBoolean(Config.CHECK_VERSION));
        warnSingVarsCBox.setSelected(userProperties.getBoolean(Config.WARN_SING_VAR));
        shortUnnamedVarCB.setSelected(userProperties.getBoolean(Config.SHORT_UNNAMED_VARS));
        jadeSnifferCB.setSelected(userProperties.getBoolean(Config.JADE_SNIFFER));
        jadeRmaCB.setSelected(userProperties.getBoolean(Config.JADE_RMA));

        for (String i: userProperties.getAvailableInfrastructures()) {
            infraTP.append(i+"="+userProperties.getInfrastructureFactoryClass(i)+"\n");
        }

        return pop;
    }

    protected JButton createBrowseButton(final String jarfile, final JTextField tf) {
        JButton bt = new JButton("Browse");
        bt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                    chooser.setDialogTitle("Select the "+jarfile+" file");
                    chooser.setFileFilter(new JarFileFilter("The "+jarfile+" file"));
                    //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        String selJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
                        if (Config.checkJar(selJar)) {
                            tf.setText(selJar);
                        } else {
                            JOptionPane.showMessageDialog(null, "The selected "+jarfile+" file was not ok!");
                        }
                    }
                } catch (Exception e) {}
            }
        });
        return bt;
    }

    public void save() {
        if (Config.checkJar(jadeJarTF.getText())) {
            userProperties.put(Config.JADE_JAR, jadeJarTF.getText().trim());
        }
        userProperties.put(Config.JADE_ARGS, jadeArgsTF.getText().trim());

        if (Config.checkJar(jasonTF.getText())) {
            userProperties.put(Config.JASON_JAR, jasonTF.getText().trim());
        }

        if (Config.checkJavaHomePath(javaTF.getText()) || Config.checkJREHomePath(javaTF.getText())) {
            userProperties.setJavaHome(javaTF.getText().trim());
        }

        if (Config.checkAntLib(antTF.getText())) {
            userProperties.setAntLib(antTF.getText().trim());
        }

        //userProperties.put(Config.SHELL_CMD, shellTF.getText().trim());
        //userProperties.put(Config.RUN_AS_THREAD, insideJIDECBox.isSelected()+"");
        userProperties.put(Config.CLOSEALL, closeAllCBox.isSelected()+"");
        userProperties.put(Config.CHECK_VERSION, checkVersionCBox.isSelected()+"");
        userProperties.put(Config.WARN_SING_VAR, warnSingVarsCBox.isSelected()+"");
        userProperties.put(Config.SHORT_UNNAMED_VARS, shortUnnamedVarCB.isSelected()+"");
        userProperties.put(Config.JADE_SNIFFER, jadeSnifferCB.isSelected()+"");
        userProperties.put(Config.JADE_RMA, jadeRmaCB.isSelected()+"");

        // infras
        BufferedReader in = new BufferedReader(new StringReader(infraTP.getText()));
        String i;
        try {
            for (String s: userProperties.getAvailableInfrastructures()) {
                userProperties.removeInfrastructureFactoryClass(s);
            }
            while ( (i = in.readLine()) != null) {
                int pos = i.indexOf("=");
                if (pos > 0) {
                    String infra = i.substring(0,pos);
                    String factory = i.substring(pos+1);
                    userProperties.setInfrastructureFactoryClass(infra, factory);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        userProperties.store();
    }

    class JarFileFilter extends FileFilter {
        String ds;
        public JarFileFilter(String ds) {
            this.ds  = ds;
        }
        public boolean accept(File f) {
            if (f.getName().endsWith("jar") || f.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        public String getDescription() {
            return ds;
        }
    }
}
