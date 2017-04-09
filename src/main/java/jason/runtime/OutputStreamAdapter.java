package jason.runtime;


import java.io.PrintStream;

import javax.swing.JTextArea;

/** adapts an output print stream to a GUI interface (MasConsole or some JTextArea) */
public class OutputStreamAdapter extends PrintStream {

    private MASConsoleGUI masConsole;
    private JTextArea     ta;

    public PrintStream originalOut = null;
    public PrintStream originalErr = null;

    public OutputStreamAdapter(MASConsoleGUI m, JTextArea t) {
        super(System.out);
        masConsole = m;
        ta = t;
    }

    public void setAsDefaultOut() {
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(this);
        System.setErr(this);
    }

    public void restoreOriginalOut() {
        if (originalOut != null) {
            System.setOut(originalOut);
        }
        if (originalErr != null) {
            System.setErr(originalErr);
        }
    }


    void append(String s) {
        if (masConsole != null) {
            masConsole.append(s);
        }
        if (ta != null) {
            ta.append(s);
            ta.setCaretPosition(ta.getDocument().getLength());
        }
    }

    public void print(Object s) {
        append(s.toString());
    }
    public void println(Object s) {
        append(s+"\n");
    }

    public void print(String s) {
        append(s.toString());
    }
    public void println(String s) {
        append(s+"\n");
    }

    public void print(boolean arg) {
        append(arg+"");
    }
    public void print(char arg0) {
        append(arg0+"");
    }
    public void print(double arg0) {
        append(arg0+"");
    }
    public void print(float arg0) {
        append(arg0+"");
    }
    public void print(int arg0) {
        append(arg0+"");
    }
    public void print(long arg0) {
        append(arg0+"");
    }
    public void println(boolean arg0) {
        append(arg0+"\n");
    }
    public void println(char arg0) {
        append(arg0+"\n");
    }
    public void println(double arg0) {
        append(arg0+"\n");
    }
    public void println(float arg0) {
        append(arg0+"\n");
    }
    public void println(int arg0) {
        append(arg0+"\n");
    }
    public void println(long arg0) {
        append(arg0+"\n");
    }
    public void println() {
        append("\n");
    }

    public String toString() {
        String s = "masConsole";
        if (masConsole == null) {
            s = "textArea";
        }
        return "OutputAdapter("+s+")";
    }
}
