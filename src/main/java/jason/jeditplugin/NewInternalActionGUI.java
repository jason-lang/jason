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

public class NewInternalActionGUI extends NewAgentGUI {

    private static final long serialVersionUID = 1L;

    private JTextField iaClass;

    private JTextField iaPkg;

    public NewInternalActionGUI(String title, Buffer b, View view) {
        super(title, b, view, ".");
    }

    protected void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        // Fields
        iaPkg = new JTextField(20);
        createField("Java package", iaPkg, "Enter the name of the Java package of the new internal action here.");

        iaClass = new JTextField(20);
        createField("Internal action name", iaClass, "Enter the name of the new internal action class here.");

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "New internal action parameters", TitledBorder.LEFT, TitledBorder.TOP));
        p.add(pLabels, BorderLayout.CENTER);
        p.add(pFields, BorderLayout.EAST);

        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    protected boolean ok() {
        if (iaPkg.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "A package name must be informed.");
            return false;
        }
        if (iaClass.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "A name for the internal action must be informed.");
            return false;
        }

        String pck = iaPkg.getText().trim();
        String pckDir = pck.replace('.', '/');
        try {
            // to create directory
            new File(buffer.getDirectory() + pckDir).mkdirs();
        } catch (Exception e) {
        }

        String ia = iaClass.getText().trim();
        if (Character.isUpperCase(ia.charAt(0))) {
            ia = Character.toLowerCase(ia.charAt(0)) + ia.substring(1);
        }

        // create new agent buffer
        String iaFile = buffer.getDirectory() + pckDir + File.separator + ia + ".java";
        boolean newFile = !new File(iaFile).exists();

        Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, iaFile);
        if (newFile) {
            try {
                nb.writeLock();
                nb.insert(0, getIAText(pck, ia));
                nb.save(view, iaFile);
            } finally {
                nb.writeUnlock();
            }
        }
        return true;
    }

    String getIAText(String pck, String className) {
        String iaCode = Config.get().getTemplate("ia");
        iaCode = iaCode.replaceAll("<IA_NAME>", className);
        iaCode = iaCode.replaceAll("<PCK>", pck);
        iaCode = iaCode.replaceAll("<PROJECT_NAME>", buffer.getName());
        iaCode = iaCode.replaceAll("<SUPER_CLASS>", "DefaultInternalAction");
        return iaCode;
    }
}
