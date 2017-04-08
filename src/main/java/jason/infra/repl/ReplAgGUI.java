package jason.infra.repl;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ReplAgGUI extends ReplAg {

    JTextField command = null;
    JTextArea output = null;
    JFrame frame;

    //asl2html agTransformer = new asl2html("/xml/agInspection.xsl");
    //JTextPane mindPanel = null;

    @Override
    public void initAg() {
        super.initAg();
        initGui();
        initLogger();
        /*if (mindPanel != null) {
            new Thread("update mind thread") {
                public void run() {
                    while (getTS().getUserAgArch().isRunning()) {
                        try {
                            updateMindView();
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }.start();
        }*/
    }

    void initLogger() {
        getTS().getLogger().addHandler( new StreamHandler() {
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
        command.setToolTipText("Type a Jason operation here.");
        command.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                execCmd(command.getText().trim());
            }
        });

        //mindPanel = new JTextPane();
        //mindPanel.setEditable(false);
        //mindPanel.setContentType("text/html");

        output = new JTextArea(5,50);
        output.setFont(font);
        output.setEditable(false);
        output.setText("Example of operations you can type:\n   +bel; !goal; .add_plan({+!goal <- .print(ok) }); !!goal; \n   .send(bob,tell,hello);\n");
        output.append("   ?bel(A); .findall(X,bel(X),L); \n");
        output.append("   .mi // to open mind inspector\n");
        output.append("   .verbose(2) // to show debug messages\n");
        output.append("   .clear // clean console\n");
        output.append("\nYou can add more agents using the button 'new REPL ag' in MAS Console.");

        output.append("\n");


        frame = new JFrame(".::  REPL Interface for "+getTS().getUserAgArch().getAgName()+"  ::.");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(BorderLayout.NORTH,command);
        //f.getContentPane().add(BorderLayout.CENTER, new JScrollPane(mindPanel));
        frame.getContentPane().add(BorderLayout.CENTER,new JScrollPane(output));

        frame.pack();
        int h = 200;
        int w = (int)(h*2*1.618);
        frame.setBounds((int)(h*0.618), 20, w, h);
        frame.setLocation(lastPos, 200+lastPos);
        lastPos += 50;
        frame.setVisible(true);
    }

    @Override
    public void clear() {
        output.setText("");
    }

    @Override
    public void print(String s) {
        output.append(s+"\n");
    }

    @Override
    public void stopAg() {
        frame.setVisible(false);
        super.stopAg();
    }

    /*
    private String lastMind = "";

    void updateMindView() {
        getTS().getUserAgArch().setCycleNumber(cmdCounter);
        Document agState = getAgState(); // the XML representation of the agent's mind
        try {
            String sMind = agTransformer.transform(agState); // transform to HTML
            if (!sMind.equals(lastMind))
                mindPanel.setText(sMind); // show the HTML in the screen
            lastMind = sMind;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
