import jason.control.ExecutionControl;
import jason.util.asl2html;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JTextPane;

import org.w3c.dom.Document;


public class MyController extends ExecutionControl {

    // GUI components
    asl2html agTransformer = new asl2html("/xml/agInspection-nd.xsl");
    static int NBAG = 4;
    JFrame frame;
    JTextPane[] panels = new JTextPane[NBAG];

    public MyController() {
        createGUI();
    }

    @Override
    protected void allAgsFinished() { // this method is called when all agents have finished one reasoning cycle
        super.allAgsFinished();
        try {
            Collection<String> ags = getExecutionControlInfraTier().getRuntimeServices().getAgentsNames(); // get the name of all agents
            int i = 0;
            for (String ag: ags) {
                Document agState = getExecutionControlInfraTier().getAgState(ag); // the XML representation of the agent's mind
                String sMind = agTransformer.transform(agState); // transform to HTML
                panels[i++].setText(sMind); // show the HTML in the screen
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createGUI() {
        frame = new JFrame("MyController");

        // create space for 4 agents
        frame.getContentPane().setLayout(new GridLayout(NBAG/2,NBAG/2));
        for (int i=0; i<NBAG; i++) {
            panels[i] = new JTextPane();
            panels[i].setEditable(false);
            panels[i].setContentType("text/html");
            frame.getContentPane().add(panels[i]);
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(0, 0, screenSize.width, screenSize.height);
        frame.setVisible(true);
    }

    public void stop() {
        super.stop();
        frame.dispose();
    }

}
