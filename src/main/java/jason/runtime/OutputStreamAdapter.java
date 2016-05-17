//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

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
