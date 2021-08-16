package jason.runtime;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import jason.infra.local.BaseLocalMAS;

/** the GUI console to output log messages */
public class MASConsoleGUI {

    protected static MASConsoleGUI masConsole        = null;
    public    static String        isTabbedPropField = MASConsoleLogHandler.class.getName() + ".tabbed";

    private boolean                isTabbed          = false;

    /** for singleton pattern */
    public static MASConsoleGUI get() {
        if (masConsole == null) {
            masConsole = new MASConsoleGUI("MAS Console");
        }
        return masConsole;
    }

    public static boolean hasConsole() {
        return masConsole != null;
    }

    protected Map<String, JTextArea>       agsTextArea       = new HashMap<String, JTextArea>();
    protected JTabbedPane                  tabPane;
    protected JFrame              frame   = null;
    protected JTextArea           output;
    protected JPanel              pBt     = null;
    protected JPanel              pcenter;
    protected OutputStreamAdapter out;
    protected boolean             inPause = false;

    protected MASConsoleGUI(String title) {
        initFrame(title);
        initMainPanel();
        initOutput();
        initButtonPanel();
    }

    protected void initFrame(String title) {
        frame = new JFrame(title);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        frame.getContentPane().setLayout(new BorderLayout());
        int h = 600;
        int w = (int)(h*1.618);
        frame.setBounds((int)(h*0.618), 20, w, h);
    }

    protected void initMainPanel() {
        String tabbed = LogManager.getLogManager().getProperty(isTabbedPropField);
        if (tabbed != null && tabbed.equals("true")) {
            isTabbed = true;
        }
        pcenter = new JPanel(new BorderLayout());
        if (isTabbed) {
            tabPane = new JTabbedPane(JTabbedPane.LEFT);
            pcenter.add(BorderLayout.CENTER, tabPane);
        }
        frame.getContentPane().add(BorderLayout.CENTER, pcenter);
    }

    protected void initOutput() {
        output = new JTextArea();
        output.setEditable(false);
        ((DefaultCaret)output.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        if (isTabbed) {
            tabPane.add("common", new JScrollPane(output));
        } else {
            pcenter.add(BorderLayout.CENTER, new JScrollPane(output));
        }
    }

    public void cleanConsole() {
        output.setText("");
    }

    protected void initButtonPanel() {
        pBt = new JPanel();
        pBt.setLayout(new FlowLayout(FlowLayout.CENTER));

        frame.getContentPane().add(BorderLayout.SOUTH, pBt);

        JButton btClean = new JButton("Clean", new ImageIcon(BaseLocalMAS.class.getResource("/images/clear.gif")));
        btClean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cleanConsole();
            }
        });

        addButton(btClean);
    }

    public void setTitle(String s) {
        frame.setTitle(s);
    }

    public JFrame getFrame() {
        return frame;
    }

    public void addButton(JButton jb) {
        pBt.add(jb);
        pBt.revalidate();
        // pack();
    }

    synchronized public void setPause(boolean b) {
        inPause = b;
        notifyAll();
    }

    synchronized void waitNotPause() {
        try {
            while (inPause) {
                wait();
            }
        } catch (Exception e) { }
    }

    public boolean isTabbed() {
        return isTabbed;
    }
    public boolean isPause() {
        return inPause;
    }

    public void append(String s) {
        append(null, s);
    }

    public void append(final String agName, String s) {
        try {
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }
            if (inPause) {
                waitNotPause();
            }
            if (isTabbed && agName != null) {
                JTextArea ta = agsTextArea.get(agName);
                if (ta == null) {
                    ta = new JTextArea();
                    ta.setEditable(false);
                    ((DefaultCaret)ta.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                    final JTextArea cta = ta;
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                agsTextArea.put(agName, cta);
                                tabPane.add(agName, new JScrollPane(cta));
                            }
                        });
                    } catch (InterruptedException e) {
                        return; // ignore the append
                    }
                }
                if (ta != null) {
                    if (ta.getDocument().getLength() > 100000) {
                        ta.setText("");
                    }
                    ta.append(s);
                }
            }

            // print in output
            synchronized (output) {
                try {
                    if (output.getDocument().getLength() > 60000) {
                        cleanConsole();
                    }
                    output.append(s);
                } catch (IllegalArgumentException e) {
                }
            }
        } catch (Exception e) {
            try {
                PrintWriter out = new PrintWriter(new FileWriter("e_r_r_o_r.txt"));
                out.write("Error that can not be printed in the MAS Console!\n"+e.toString()+"\n");
                e.printStackTrace(out);
                out.close();
            } catch (IOException e1) {
            }
        }
    }

    public void close() {
        setPause(false);
        if (masConsole != null && masConsole.frame != null)
            masConsole.frame.setVisible(false);
        if (out != null)
            out.restoreOriginalOut();
        try {
            if (BaseLocalMAS.getRunner() != null) {
                FileWriter f = new FileWriter(BaseLocalMAS.stopMASFileName);
                f.write(32);
                f.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        masConsole = null;
    }

    public void setAsDefaultOut() {
        out = new OutputStreamAdapter(this, null);
        out.setAsDefaultOut();
    }

}
