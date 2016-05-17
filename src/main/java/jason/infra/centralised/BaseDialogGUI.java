package jason.infra.centralised;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** base class for dialog windows */
abstract public class BaseDialogGUI extends JDialog {

    protected JButton ok;
    protected JButton cancel;
    
    protected JPanel pFields = new JPanel(new GridLayout(0,1));
    protected JPanel pLabels = new JPanel(new GridLayout(0,1));

    public BaseDialogGUI(Frame f, String title) {
        super(f);
        setTitle(title);
        initComponents();
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int)( (screenSize.width - getWidth()) / 2),(int) ((screenSize.height - getHeight())/2));
        setVisible(true);
    }
    
    abstract protected void initComponents();

    protected JPanel createButtonsPanel() {
        // Buttons
        JPanel bts = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ok = new JButton("Ok");
        ok.setDefaultCapable(true);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (ok()) {
                    setVisible(false);
                }
            }
        });
        bts.add(ok);
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });
        bts.add(cancel);
        getRootPane().setDefaultButton(ok);
        return bts;
    }

    protected void createField(String label, JComponent tf, String tooltip) {
        JLabel jl = new JLabel(label+": ");
        jl.setToolTipText(tooltip);
        tf.setToolTipText(tooltip);
        pLabels.add(jl);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(tf);
        pFields.add(p);
    }

    protected void createField(String label, JComponent tf1, JComponent tf2, String tooltip) {
        JLabel jl = new JLabel(label+": ");
        jl.setToolTipText(tooltip);
        tf1.setToolTipText(tooltip);
        pLabels.add(jl);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(tf1);
        p.add(tf2);
        pFields.add(p);
    }
    
    abstract protected boolean ok();
    
}
