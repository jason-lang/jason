package jason.architecture;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.w3c.dom.Document;

import jason.NoValueException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.control.ExecutionControlGUI;
import jason.runtime.Settings;
import jason.util.Config;
import jason.util.asl2html;
import jason.util.asl2xml;

/**
 * ArArch that displays the mind state of the agent
 */
public class MindInspectorAgArch extends AgArch {

    // variables for mind inspector
    protected boolean hasMindInspectorByCycle = false;
    protected int     updateInterval = 0;
    protected static JFrame         mindInspectorFrame = null;
    protected static JTabbedPane    mindInspectorTab = null;
    protected        JTextPane      mindInspectorPanel = null;
    protected        JSlider        mindInspectorHistorySlider = null;
    protected        JCheckBox      mindInspectorFreeze = null;
    protected        List<Document> mindInspectorHistory = null;
    protected        asl2xml        mindInspectorTransformer = null;

    protected        String         mindInspectorDirectory;

    // Which item is to be shown in HTML interface
    Map<String,Boolean> show = new HashMap<String,Boolean>();

    // what is currently shown
    Document agState = null;

    MindInspectorWeb webServer = null;
    boolean hasHistory = false;

    @Override
    public void init() {
        setupMindInspector(getTS().getSettings().getUserParameter(Settings.MIND_INSPECTOR));
    }

    /**
     * A call-back method called by the infrastructure tier
     * when the agent is about to be killed.
     */
    @Override
    public void stop() {
        if (mindInspectorFrame != null)
            mindInspectorFrame.dispose();
        super.stop();
    }

    @Override
    public void reasoningCycleStarting() {
        if (hasMindInspectorByCycle)
            addAgState();
        super.reasoningCycleStarting();
    }



    /**
     *    process the mindinspector parameter used in the agent option in .mas2j project.
     *    E.g. agents bob x.asl [mindinspector="gui(cycle,html)"];
     *
     *    General syntax of the parameter:
     *    [gui|file|web] ( [ cycle|number ] , [xml,html] [, history | directory] )
     */
    protected void setupMindInspector(String configuration) {
        Structure sConf = null;
        try {
            sConf = ASSyntax.parseStructure(configuration);
        } catch (Exception e) {
            getTS().getLogger().warning("The mindinspector argument does not parse as a predicate! "+configuration+" --  (see Jason FAQ) -- error: "+e);
            return;
        }

        // get the frequency of updates
        hasMindInspectorByCycle = sConf.getTerm(0).toString().equals("cycle");

        if (! hasMindInspectorByCycle) {
            try {
                updateInterval = (int)((NumberTerm)sConf.getTerm(0)).solve();
            } catch (NoValueException e1) {
                e1.printStackTrace();
            }
            new Thread("update agent mind inspector") {
                public void run() {
                    try {
                        while (isRunning()) {
                            Thread.sleep(updateInterval);
                            addAgState();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
            } .start();
        }

        if (sConf.getFunctor().equals("gui")) {
            createGUIMindInspector(sConf);
        } else if (sConf.getFunctor().equals("file")) {
            createFileMindInspector(sConf);
        } else if (sConf.getFunctor().equals("web")) {
            createWebMindInspector(sConf);
        }
    }

    private void createGUIMindInspector(Structure sConf) {
        // assume html output
        String format = "text/html";

        if (mindInspectorFrame == null) { // Initiate the common window
            mindInspectorFrame = new JFrame(ExecutionControlGUI.title);
            mindInspectorTab   = new JTabbedPane(JTabbedPane.LEFT);
            mindInspectorFrame.getContentPane().add(mindInspectorTab);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            mindInspectorFrame.setBounds(100, 200, (int)((screenSize.width-100)*0.7), (int)((screenSize.height-100)*0.9));
            mindInspectorFrame.setVisible(true);
        }

        mindInspectorPanel = new JTextPane();
        mindInspectorPanel.setEditable(false);
        mindInspectorPanel.setContentType(format);
        mindInspectorPanel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                hyperLink(evt);
            }
        });
        show.put("bels", true);
        show.put("annots", Config.get().getBoolean(Config.SHOW_ANNOTS));
        show.put("rules", false);
        show.put("evt", true);
        show.put("mb", false);
        show.put("int", false);
        show.put("int-details", false);
        show.put("plan", false);
        show.put("plan-details", false);

        // get history
        boolean hasHistory = sConf.getArity() == 3 && sConf.getTerm(2).toString().equals("history");
        if (! hasHistory) {
            mindInspectorTab.add(getAgName(), new JScrollPane(mindInspectorPanel));
        } else {
            mindInspectorHistory = new ArrayList<Document>();
            JPanel pHistory = new JPanel(new BorderLayout());//new FlowLayout(FlowLayout.CENTER));
            pHistory.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Agent History", TitledBorder.LEFT, TitledBorder.TOP));
            mindInspectorHistorySlider = new JSlider();
            mindInspectorHistorySlider.setMaximum(1);
            mindInspectorHistorySlider.setMinimum(0);
            mindInspectorHistorySlider.setValue(0);
            mindInspectorHistorySlider.setPaintTicks(true);
            mindInspectorHistorySlider.setPaintLabels(true);
            mindInspectorHistorySlider.setMajorTickSpacing(10);
            mindInspectorHistorySlider.setMinorTickSpacing(1);
            setupSlider();
            mindInspectorHistorySlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    try {
                        int c = (int)mindInspectorHistorySlider.getValue();
                        showAgState(mindInspectorHistory.get(c));
                    } catch (Exception e2) {                        }
                }
            });
            pHistory.add(BorderLayout.CENTER, mindInspectorHistorySlider);

            mindInspectorFreeze = new JCheckBox();
            JPanel pf = new JPanel(new FlowLayout());
            pf.add(mindInspectorFreeze);
            pf.add(new JLabel("freeze"));
            pHistory.add(BorderLayout.EAST, pf);

            JPanel pAg = new JPanel(new BorderLayout());
            pAg.add(BorderLayout.CENTER, new JScrollPane(mindInspectorPanel));
            pAg.add(BorderLayout.SOUTH, pHistory);
            mindInspectorTab.add(getAgName(), pAg);
        }

        if (format.equals("text/html")) {
            mindInspectorTransformer = new asl2html("/xml/agInspection.xsl");
        }
    }

    private void hyperLink(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String uri = "show?";
            int pos = evt.getDescription().indexOf(uri);
            if (pos >= 0) {
                String par = evt.getDescription().substring(pos+uri.length());
                show.put(par,true);
            } else {
                uri = "hide?";
                pos = evt.getDescription().indexOf(uri);
                if (pos >= 0) {
                    String par = evt.getDescription().substring(pos+uri.length());
                    show.put(par,false);
                }
            }
            showAgState(agState);
        }
    }

    private void setupSlider() {
        int size = mindInspectorHistory.size()-1;
        if (size < 0)
            return;

        Hashtable<Integer,Component> labelTable = new Hashtable<Integer,Component>();
        labelTable.put( 0, new JLabel("mind 0") );
        labelTable.put( size, new JLabel("mind "+size) );
        mindInspectorHistorySlider.setLabelTable( labelTable );
        mindInspectorHistorySlider.setMaximum(size);
        //mindInspectorHistorySlider.setValue(size);
    }


    private void createFileMindInspector(Structure sConf) {
        if (sConf.getArity() <= 2)
            mindInspectorDirectory = "log";
        else
            mindInspectorDirectory = sConf.getTerm(2).toString();

        // assume xml output
        mindInspectorTransformer = new asl2xml();

        // create directories
        mindInspectorDirectory += "/"+getAgName();
        File dirmind = new File(mindInspectorDirectory);
        if (!dirmind.exists()) // create agent dir
            dirmind.mkdirs();

        // create a directory for this execution
        int c = 0;
        String d = mindInspectorDirectory+"/run-"+c;
        while (new File(d).exists()) {
            d = mindInspectorDirectory+"/run-"+(c++);
        }
        mindInspectorDirectory = d;
        new File(mindInspectorDirectory).mkdirs();
    }

    private void createWebMindInspector(Structure sConf) {
        webServer = MindInspectorWeb.get();
        hasHistory = sConf.getArity() == 3 && sConf.getTerm(2).toString().equals("history");
        mindInspectorTransformer = new asl2html("/xml/agInspection.xsl");
    }


    private String lastHistoryText = "";
    private int    fileCounter = 0;

    protected void addAgState() {
        try {
            Document state = getTS().getAg().getAgState(); // the XML representation of the agent's mind
            String sMind = getAgStateAsString(state, true);
            if (sMind.equals(lastHistoryText))
                return;
            lastHistoryText = sMind;

            if (mindInspectorPanel != null) { // output on GUI
                if (mindInspectorFreeze == null || !mindInspectorFreeze.isSelected())
                    showAgState(state);
                if (mindInspectorHistory != null) {
                    mindInspectorHistory.add(state);
                    setupSlider();
                    mindInspectorHistorySlider.setValue(mindInspectorHistory.size()-1);
                }
            } else if (mindInspectorDirectory != null) { // output on file
                String filename = String.format("%6d.xml",fileCounter++).replaceAll(" ","0");
                FileWriter outmind = new FileWriter(new File(mindInspectorDirectory+"/"+filename));
                outmind.write(sMind);
                outmind.close();
            } else if (webServer != null) { // output on web
                webServer.addAgState(getTS().getAg(), state, hasHistory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String previousShownText = "";
    /** show current agent state */
    void showAgState(Document state) { // in GUI
        if (state != null) {
            try {
                String sMind = getAgStateAsString(state, false);
                if (sMind.equals(previousShownText))
                    return; // nothing to log
                previousShownText = sMind;
                agState = state;
                mindInspectorPanel.setText(sMind); // show the HTML in the screen
            } catch (Exception e) {
                mindInspectorPanel.setText("Error in XML transformation!" + e);
                e.printStackTrace();
            }
        }
    }

    String getAgStateAsString(Document ag, boolean full) { // full means with show all
        try {
            for (String p: show.keySet())
                if (full)
                    mindInspectorTransformer.setParameter("show-"+p, "true");
                else
                    mindInspectorTransformer.setParameter("show-"+p, show.get(p)+"");
            return mindInspectorTransformer.transform(ag); // transform to HTML
        } catch (Exception e) {
            if (mindInspectorPanel != null) {
                mindInspectorPanel.setText("Error in XML transformation!" + e);
            }
            e.printStackTrace();
            return "Error XML transformation (MindInspector)";
        }
    }

}
