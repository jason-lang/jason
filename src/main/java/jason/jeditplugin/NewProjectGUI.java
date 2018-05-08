package jason.jeditplugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;

import jason.mas2j.MAS2JProject;
import jason.util.Config;

public class NewProjectGUI extends NewAgentGUI {

    private static final long serialVersionUID = 1L;

    JTextField projName;

    JTextField projDir;

    JLabel     projFinalDir;

    // JTextField projEnv;
    JComboBox  projInfra;

    JasonID    jasonID;

    public NewProjectGUI(String title, View view, JasonID jasonID) {
        super(title, null, view, ".");
        this.jasonID = jasonID;
    }

    protected void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        // Fields
        projName = new JTextField(10);
        createField("Project name", projName, "Enter he project name here");

        // projEnv = new JTextField(20);
        // createField("Environment class", projEnv, "The java class that
        // implements the environment (<package.classname>). If not filled, the
        // default class will be used.");

        projInfra = new JComboBox(Config.get().getAvailableInfrastructures());
        projInfra.setSelectedIndex(0);
        createField("Infrastructure", projInfra, "Set the Infrastructure");

        JPanel jasonHomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pLabels.add(new JLabel("Root location:"));
        projDir = new JTextField(20);
        projDir.setText(System.getProperty("user.home"));
        jasonHomePanel.add(projDir);
        JButton setDir = new JButton("Browse");
        setDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
                    chooser.setDialogTitle("Select the project directory");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File projDirectory = chooser.getSelectedFile();
                        if (projDirectory.isDirectory()) {
                            projDir.setText(projDirectory.getCanonicalPath());
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
        jasonHomePanel.add(setDir);
        pFields.add(jasonHomePanel);

        projFinalDir = new JLabel();
        createField("Directory", projFinalDir, "This is the directory that will be created for the new project");
        // doc listener for Final proj dir
        DocumentListener docLis = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateProjDir();
            }

            public void removeUpdate(DocumentEvent e) {
                updateProjDir();
            }

            public void changedUpdate(DocumentEvent e) {
                updateProjDir();
            }
        };
        projName.getDocument().addDocumentListener(docLis);
        projDir.getDocument().addDocumentListener(docLis);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "New project parameters", TitledBorder.LEFT, TitledBorder.TOP));
        p.add(pLabels, BorderLayout.CENTER);
        p.add(pFields, BorderLayout.EAST);

        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    void updateProjDir() {
        String s = "";
        if (!projDir.getText().endsWith(File.separator)) {
            s = File.separator;
        }
        projFinalDir.setText(projDir.getText() + s + projName.getText());
    }

    protected boolean ok() {
        String projDecl = getProjDecl();
        if (projDecl == null) {
            return false;
        }

        File finalDir = new File(projFinalDir.getText().trim());
        try {
            if (!finalDir.exists()) {
                boolean ok = finalDir.mkdirs();
                if (!ok)
                    JOptionPane.showMessageDialog(this, "Error creating project directory: "+finalDir);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating project directory: "+finalDir+": " + e);
            return false;
        }

        String pFile = finalDir + File.separator + projName.getText() + "." + MAS2JProject.EXT;
        boolean newFile = !new File(pFile).exists();
        Buffer b = org.gjt.sp.jedit.jEdit.openFile(view, pFile);
        if (newFile) {
            try {
                b.writeLock();
                b.insert(0, projDecl);
                b.save(view, pFile);
            } finally {
                b.writeUnlock();
            }
        }
        /*
         * jasonID.checkProjectView(projName.getText(), finalDir);
         *
         * DockableWindowManager d = view.getDockableWindowManager(); if
         * (d.getDockableWindow("projectviewer") != null) { if
         * (!d.isDockableWindowVisible("projectviewer")) {
         * d.addDockableWindow("projectviewer"); } }
         */

        jasonID.textArea.setText("Project created!");
        return true;
    }

    private String getProjDecl() {
        if (projName.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "A project name must be informed.");
            return null;
        }
        String name = projName.getText().trim();
        if (Character.isUpperCase(name.charAt(0))) {
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        if (jasonID.getProjectBuffer(name + "." + MAS2JProject.EXT) != null) {
            JOptionPane.showMessageDialog(this, "There already is a project called " + name);
            return null;
        }

        String projDecl = Config.get().getTemplate("project");
        projDecl = projDecl.replace("<PROJECT_NAME>", name);
        projDecl = projDecl.replace("<INFRA>", projInfra.getSelectedItem().toString());
        projDecl = projDecl.replace("<VERSION>", Config.get().getJasonVersion());
        projDecl = projDecl.replace("<DATE>", new SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss").format(new Date()));
        return projDecl;
    }
}
