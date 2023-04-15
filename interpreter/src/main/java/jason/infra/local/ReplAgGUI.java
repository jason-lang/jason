package jason.infra.local;

import jason.architecture.MindInspectorWeb;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.stdlib.print_unifier;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class ReplAgGUI {

    JTextField command = null;
    JTextArea output = null;
    JFrame frame;

    Agent ag = null;

    public void init(Agent  ag) {
        this.ag = ag;
        initGui();
        initLogger();
    }

    void initLogger() {
        ag.getTS().getLogger().addHandler( new StreamHandler() {
            @Override
            public synchronized void publish(LogRecord l) {
                output.append(l.getMessage()+"\n");
                output.setCaretPosition( output.getDocument().getLength());
            }
        });
    }

    static int lastPos = 30;

    void initGui() {
        Font font = new Font("Courier", Font.PLAIN, 14);
        command = new JTextField(40);
        command.setFont(font);
        command.setToolTipText("Type a Jason command here.");
        command.addActionListener(arg0 -> execCmd(command.getText().trim()));

        output = new JTextArea(5,50);
        output.setFont(font);
        output.setEditable(false);
        output.setText("Example of commands you can type:\n");
        output.append("   +bel(a)\n");
        output.append("   ?bel(A)\n");
        output.append("   .send(bob,tell,hello)\n");
        output.append("   .findall(X,bel(X),L); \n");
        output.append("   .add_plan({+!goal <- .print(ok) })\n");
        output.append("   !goal\n");
        output.append("   .mi // to open mind inspector\n");
        output.append("   .verbose(2) // to show debug messages\n");
        output.append("   .clear // clean console\n");

        output.append("\n");

        frame = new JFrame(".::  REPL Interface for "+ag.getTS().getAgArch().getAgName()+"  ::.");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(BorderLayout.NORTH,command);
        frame.getContentPane().add(BorderLayout.CENTER,new JScrollPane(output));

        frame.pack();
        int h = 300;
        int w = (int)(h*1.618);
        frame.setBounds((int)(h*0.618), 20, w, h);
        frame.setLocation(lastPos, 200+lastPos);
        lastPos += 50;
        frame.setVisible(true);
    }

    void execCmd(String sCmd) {
        if (!ag.getTS().getAgArch().isRunning()) {
            output.setText("the agent is not running anymore!");
            return;
        }
        try {
            if (sCmd.endsWith("."))
                sCmd = sCmd.substring(0,sCmd.length()-1);

            if (sCmd.equals(".clear")) {
                output.setText("");
                return;
            }
            if (sCmd.startsWith(".verbose")) {
                var l = ASSyntax.parseLiteral(sCmd);
                setVerbose( (int)((NumberTerm)l.getTerm(0)).solve());
                return;
            }
            if (sCmd.equals(".mi")) {
                String url = MindInspectorWeb.getURL()+"/agent-mind/"+ag.getTS().getAgArch().getAgName();
                Desktop.getDesktop().browse(new URI(url));
                return;
            }

            sCmd += "; "+ print_unifier.class.getName();
            PlanBody lCmd = ASSyntax.parsePlanBody(sCmd);
            Trigger te   = ASSyntax.parseTrigger("+!run_repl_expr");
            Intention i   = new Intention();
            i.push(new IntendedMeans(
                    new Option(
                            new Plan(null,te,null,lCmd),
                            new Unifier()),
                    te));
            ag.getTS().getC().addRunningIntention(i);
            ag.getTS().getAgArch().wake();
        } catch (Exception e) {
            output.append("Error parsing "+sCmd+"\n"+e);
        }
    }


    void setVerbose(int v) {
        switch (v) {
            case 0:
                ag.getTS().getLogger().setLevel(Level.SEVERE);
                break;
            case 1:
                ag.getTS().getLogger().setLevel(Level.INFO);
                break;
            case 2:
                ag.getTS().getLogger().setLevel(Level.FINE);
                break;
        }
    }
}
