package jason.runtime;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * A quick implementation of a TextPane with default coloring for Jason.

 * @author Felipe Meneguzzi
 */
public class MASConsoleColorGUI extends MASConsoleGUI {
    private Map<String, MASColorTextPane>     agsTextArea       = new HashMap<String, MASColorTextPane>();
    private Map<String, Color>                agsColours        = new HashMap<String, Color>();
    private MASColorTextPane                  output;

    private MASConsoleColorGUI(String title) {
        super(title);
    }

    /** for singleton pattern */
    public static MASConsoleGUI get() {
        if (masConsole == null) {
            masConsole = new MASConsoleColorGUI("MAS Console");
        }
        return masConsole;
    }

    @Override
    public void cleanConsole() {
        output.setText("");
    }

    @Override
    protected void initOutput() {
        output = new MASColorTextPane(Color.black);
        output.setEditable(false);
        ((DefaultCaret)output.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        if (isTabbed()) {
            tabPane.add(" all", new JScrollPane(output));
        } else {
            pcenter.add(BorderLayout.CENTER, new JScrollPane(output));
        }
    }

    @Override
    public void append(final String agName, String s) {
        try {
            Color c = null;
            if (agName != null) {
                c = agsColours.get(agName);
                if (c == null) {
                    c = MASColorTextPane.getNextAvailableColor();
                    agsColours.put(agName, c);
                }
            }
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }
            if (inPause) {
                waitNotPause();
            }
            if (isTabbed() && agName != null) {
                MASColorTextPane ta = agsTextArea.get(agName);
                if (ta == null) {
                    // add new tab for the agent
                    ta = new MASColorTextPane(Color.black);
                    ta.setEditable(false);
                    ((DefaultCaret)ta.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                    final MASColorTextPane cta = ta;
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            agsTextArea.put(agName, cta);
                            int i = 0;
                            for (; i<tabPane.getTabCount(); i++) {
                                if (agName.toUpperCase().compareTo( tabPane.getTitleAt(i).toUpperCase()) < 0)
                                    break;
                            }
                            tabPane.add(new JScrollPane(cta),i);
                            tabPane.setTitleAt(i, agName);
                        }
                    });
                }
                if (ta != null) {
                    // print out
                    if (ta.getDocument().getLength() > 100000) {
                        ta.setText("");
                    }
                    ta.append(s);
                }
            }

            // print in output
            synchronized (output) {
                if (output.getDocument().getLength() > 60000) {
                    cleanConsole();
                }
                try {
                    output.append(c, s);
                } catch (Throwable e) {
                    // just try again once...
                    try {
                        output.append(c, s);
                    } catch (Throwable e2) { }
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

}

class MASColorTextPane extends JTextPane {
    protected static final Color seq[] = {//Color.black,
        Color.blue,
        Color.red,
        Color.gray,
        //Color.cyan,
        Color.magenta,
        //Color.orange,
        //Color.pink,
        //Color.yellow,
        Color.green
    };
    protected static int change = 0;
    protected static int lastColor = 0;

    public synchronized static Color getNextAvailableColor() {
        if(change > 0) {
            seq[lastColor] = (change%2 == 1)?seq[lastColor].brighter():seq[lastColor].darker();
        }
        Color c = seq[lastColor];
        lastColor = (lastColor+1)%seq.length;
        if(lastColor == 0) {
            change++;
        }
        return c;
    }

    protected Color defaultColor;

    public MASColorTextPane(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void append(String s) {
        append(defaultColor, s);
    }

    public void append(Color c, String s) {
        if (c == null)
            c = defaultColor;
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        try {
            getDocument().insertString(getDocument().getLength(), s, as);
        } catch (BadLocationException e) {
        }
    }
}

