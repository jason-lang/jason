package jason.jeditplugin;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;

import jason.util.Config;

public class NewEnvironmentGUI extends NewAgentGUI {

    private static final long serialVersionUID = 1L;

    private JTextField envClass;

    public NewEnvironmentGUI(String title, Buffer b, View view) {
        super(title, b, view, ".");
    }

    protected void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        // Fields

        envClass = new JTextField(20);
        createField("Environment class name", envClass, "Enter the name of the environment Java class here.");

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "New environment parameters", TitledBorder.LEFT, TitledBorder.TOP));
        p.add(pLabels, BorderLayout.CENTER);
        p.add(pFields, BorderLayout.EAST);

        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    protected boolean ok() {
        String env = getEnvName();
        if (env == null) {
            JOptionPane.showMessageDialog(this, "An environment name must be informed.");
            return false;
        }
        try {
            buffer.writeLock();
            String proj = buffer.getText(0, buffer.getLength());
            int pos = proj.lastIndexOf("agents");
            if (pos < 0) {
                pos = buffer.getLength();
            }
            buffer.insert(pos, "environment: " + env + "\n\n    ");
        } finally {
            buffer.writeUnlock();
        }

        // create new agent buffer
        String envFile = buffer.getDirectory() + env + ".java";
        boolean newFile = !new File(envFile).exists();

        Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, envFile);
        if (newFile) {
            try {
                nb.writeLock();
                nb.insert(0, getEnvText(env));
                nb.save(view, envFile);
            } finally {
                nb.writeUnlock();
            }
        }
        return true;
    }

    private String getEnvName() {
        String env = envClass.getText().trim();
        if (env.length() > 0) {
            return env;
        } else {
            return null;
        }
    }

    String getEnvText(String className) {
        String envCode = Config.get().getTemplate("environment");
        envCode = envCode.replaceAll("<ENV_NAME>",className);
        envCode = envCode.replaceAll("<PROJECT_NAME>", buffer.getName());
        return envCode;
    }
}
