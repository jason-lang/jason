package jason.infra.centralised;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

public class StartNewAgentGUI extends BaseDialogGUI {

    private static final long serialVersionUID = 1L;

    protected JTextField agName;
    protected JTextField agSource;
    protected JTextField archClass;
    protected JTextField agClass;
    protected JTextField nbAgs;
    protected JTextField agHost;
    protected JComboBox  verbose;
    String               openDir;

    public StartNewAgentGUI(Frame f, String title, String openDir) {
        super(f, title);
        this.openDir = openDir;
    }

    protected void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        // Fields

        agName = new JTextField(10);
        createField("Agent name", agName, "The agent name");

        agSource = new JTextField(15);
        JButton sourceBrowseBt = new JButton("Browse");
        sourceBrowseBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    JFileChooser chooser = new JFileChooser(openDir);
                    chooser.setDialogTitle("Select the AgentSpeak source file");
                    chooser.setFileFilter(new AslFileFilter());
                    // chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        agSource.setText(chooser.getSelectedFile().toString());
                    }
                } catch (Exception e) {
                }
            }
        });
        createField("Source", agSource, sourceBrowseBt, "The path for the agent's source file (e.g. ../asl/code.asl). If left empty, the file will be the agent's name + .asl.");

        agClass = new JTextField(20);
        createField("Agent class", agClass, "The customisation class for the agent (<package.classname>). If not filled, the default agent class will be used.");

        archClass = new JTextField(20);
        createField("Architecture class", archClass,
                    "The customisation class for the agent architecture (<package.classname>). If not filled, the default architecture will be used.");

        nbAgs = new JTextField(4);
        nbAgs.setText("1");
        createField("Number of agents", nbAgs, "The number of agents that will be instantiated from this declaration.");

        verbose = new JComboBox(new String[] { "no output", "normal", "debug" });
        verbose.setSelectedIndex(1);
        createField("Verbose", verbose, "Set the verbose level");

        agHost = new JTextField(10);
        agHost.setText("localhost");
        createField("Host to run", agHost, "The host where this agent will run. The infrastructure must support distributed launching.");

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Agent parameters", TitledBorder.LEFT, TitledBorder.TOP));
        p.add(pLabels, BorderLayout.CENTER);
        p.add(pFields, BorderLayout.EAST);

        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    protected boolean ok() {
        final AgentParameters ap = getAgDecl();
        if (ap == null) {
            JOptionPane.showMessageDialog(this, "An agent name must be informed.");
            return false;
        }
        if (ap.asSource == null) {
            ap.asSource = new File(ap.name + "." + MAS2JProject.AS_EXT);
        }
        if (!ap.asSource.exists()) {
            JOptionPane.showMessageDialog(this, "The source file '" + ap.asSource + "' does not exist!");
            return false;
        }
        new Thread() {
            public void run() {
                boolean debug = BaseCentralisedMAS.getRunner().isDebug();
                boolean fs = BaseCentralisedMAS.getRunner().getControllerInfraTier() != null;
                RuntimeServicesInfraTier services = BaseCentralisedMAS.getRunner().getRuntimeServices();
                try {
                    String agClass = null;
                    if (ap.agClass != null) {
                        agClass = ap.agClass.getClassName();
                    }
                    for (int i = 0; i < ap.getNbInstances(); i++) {
                        String name = ap.name;
                        if (ap.getNbInstances() > 1) {
                            name = name + (i + 1);
                        }
                        // TODO: implements bb class
                        name = services.createAgent(name, ap.asSource.getAbsolutePath(), agClass, ap.getAgArchClasses(), null, ap.getAsSetts(debug, fs), null);
                        services.startAgent(name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } .start();
        return true;
    }

    protected AgentParameters getAgDecl() {
        if (agName.getText().trim().length() == 0) {
            return null;
        }
        AgentParameters ap = new AgentParameters();
        ap.name = agName.getText().trim();
        if (Character.isUpperCase(ap.name.charAt(0))) {
            ap.name = Character.toLowerCase(ap.name.charAt(0)) + ap.name.substring(1);
        }

        if (verbose.getSelectedIndex() != 1) {
            ap.addOption("verbose", verbose.getSelectedIndex() + "");
        }

        if (agSource.getText().trim().length() > 0) {
            if (agSource.getText().startsWith(File.separator)) {
                ap.asSource = new File(agSource.getText().trim());
            } else {
                ap.asSource = new File(openDir + File.separator + agSource.getText().trim());
            }
        }

        if (archClass.getText().trim().length() > 0) {
            ap.addArchClass(archClass.getText().trim());
        }

        if (agClass.getText().trim().length() > 0) {
            ap.agClass = new ClassParameters(agClass.getText().trim());
        }
        if (!nbAgs.getText().trim().equals("1")) {
            try {
                ap.setNbInstances( Integer.parseInt(nbAgs.getText().trim()) );
            } catch (Exception e) {
                System.err.println("Number of hosts is not a number!");
            }
        }
        if (!agHost.getText().trim().equals("localhost")) {
            ap.setHost(agHost.getText().trim());
        }
        return ap;
    }

    class AslFileFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.getName().endsWith(MAS2JProject.AS_EXT) || f.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        public String getDescription() {
            return "AgentSpeak language source";
        }
    }

}
