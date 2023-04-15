package jason.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Document;

import jason.infra.local.BaseLocalMAS;
import jason.util.Config;
import jason.util.asl2html;
import jason.util.asl2tex;
import jason.util.asl2xml;

@SuppressWarnings("rawtypes")
public class ExecutionControlGUI extends ExecutionControl {

    String currentAgState = "";
    String currentAgName  = "";
    int    agShownState   = 0;

    private Map<String,Map<Integer,Document>> agsHistory = new HashMap<String,Map<Integer,Document>>();

    private int countCycles = 0; // cycles since last "Run"
    private int maxCycles = 0;   // cycles to run
    private boolean waitAllAgs     = false; // run cycles in all agents
    private boolean waitSelectedAg = false; // run cycles in selected agent
    private boolean breakpoint     = false;

    // xml components
    asl2xml  agTransformerXML  = new asl2xml();
    asl2tex  agTransformerTex  = new asl2tex("/xml/ag2tex.xsl");
    asl2html agTransformerHtml = new asl2html("/xml/agInspection-nd.xsl");
    asl2xml  agTransformer     = null;


    public ExecutionControlGUI() {
        agTransformer = agTransformerHtml;
        setRunningCycle(false);
        initComponents();
    }

    @Override
    public void init(String[] args) {
        setListOfAgsFromInfra();
    }

    // Interface components
    JFrame     frame;
    JTextField jTfSteps = null;
    JComboBox  jCbWho = null;
    JButton    jBtRun = null;
    JComboBox  jCbViewAs = null;
    JSlider    jHistory = null;
    JTextPane  jTA = null;
    JList      jList = null;
    JPanel     spList;

    DefaultListModel listModel;

    // what to show
    Document agState = null;

    // Which item is to be shown in HTML interface
    Map<String,Boolean> show = new HashMap<String,Boolean>();

    public static String title = "..:: Mind Inspector ::..";

    @SuppressWarnings("unchecked")
    void initComponents() {
        frame = new JFrame(title);

        jTfSteps = new JTextField(3);
        jTfSteps.setText("1");
        jTfSteps.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        startRun();
                    }
                });
            }
        });

        jCbWho = new JComboBox();
        jCbWho.addItem("all agents");
        jCbWho.addItem("selected agent");

        jBtRun = new JButton("Run", new ImageIcon(ExecutionControlGUI.class.getResource("/images/run.gif")));
        jBtRun.setToolTipText("Run the MAS until some agent achieve a breakpoint. Breakpoints are annotations in plans' label");
        jBtRun.setEnabled(true);
        jBtRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        startRun();
                    }
                });
            }
        });

        jCbViewAs = new JComboBox();
        jCbViewAs.addItem("html");
        jCbViewAs.addItem("xml");
        jCbViewAs.addItem("LaTeX");
        jCbViewAs.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ievt) {
                String mime = jCbViewAs.getSelectedItem().toString();
                if (mime.equals("html")) {
                    agTransformer = agTransformerHtml;
                } else if (mime.equals("xml")) {
                    agTransformer = agTransformerXML;
                } else if (mime.equals("LaTeX")) {
                    mime = "xml";
                    agTransformer = agTransformerTex;
                }
                jTA.setContentType("text/"+mime);
                previousMind = "--";
                showAgState();
            }
        });


        jTA = new JTextPane();
        jTA.setEditable(false);
        jTA.setContentType("text/html");
        jTA.setAutoscrolls(false);
        jTA.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                hyperLink(evt);
            }
        });
        jTA.setText("<html><body>Select the agent to inspect.</body></html>");

        JPanel spTA = new JPanel(new BorderLayout());
        JScrollPane scpTA = new JScrollPane(jTA);
        //scpTA.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        spTA.add(BorderLayout.CENTER, scpTA);
        spTA.setBorder(BorderFactory.createTitledBorder(BorderFactory
                       .createEtchedBorder(), "Agent Inspection", TitledBorder.LEFT, TitledBorder.TOP));

        JPanel pHistory = new JPanel(new BorderLayout());//new FlowLayout(FlowLayout.CENTER));
        pHistory.setBorder(BorderFactory.createTitledBorder(BorderFactory
                           .createEtchedBorder(), "Agent History", TitledBorder.LEFT, TitledBorder.TOP));
        jHistory = new JSlider();
        jHistory.setMaximum(1);
        jHistory.setMinimum(0);
        jHistory.setValue(0);
        jHistory.setPaintTicks(true);
        jHistory.setPaintLabels(true);
        jHistory.setMajorTickSpacing(10);
        jHistory.setMinorTickSpacing(1);
        setupSlider();
        jHistory.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int c = (int)jHistory.getValue();
                try {
                    agState = agsHistory.get(currentAgName).get(c);
                    showAgState();
                } catch (Exception ex) { }
            }
        });
        pHistory.add(BorderLayout.CENTER, jHistory);
        JPanel pAg = new JPanel(new BorderLayout());
        pAg.add(BorderLayout.CENTER, spTA);
        pAg.add(BorderLayout.SOUTH, pHistory);


        listModel = new DefaultListModel();
        jList = new JList(listModel);
        spList = new JPanel(new BorderLayout());
        spList.add(BorderLayout.CENTER, new JScrollPane(jList));
        spList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Agents", TitledBorder.LEFT, TitledBorder.TOP));
        jList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                String ag = jList.getSelectedValue().toString();
                if (!ag.equals(currentAgState)) {
                    currentAgState = ag;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            inspectAgent(currentAgState);
                        }
                    });
                }
            }
        });


        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pButtons.add(jBtRun);
        pButtons.add(jTfSteps);
        pButtons.add(new JLabel("cycle(s) for "));
        pButtons.add(jCbWho);
        pButtons.add(new JLabel("        view as:"));
        pButtons.add(jCbViewAs);
        //pButtons.add(jBtRefresh);

        JSplitPane splitPaneHor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPaneHor.setTopComponent(spList);
        splitPaneHor.setDividerLocation(100);
        splitPaneHor.setBottomComponent(pAg);
        splitPaneHor.setOneTouchExpandable(true);
        //splitPane.setPreferredSize(new Dimension(600, 300));

        //JSplitPane splitPaneVer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        //splitPaneVer.setTopComponent(splitPaneHor);
        //splitPaneVer.setBottomComponent(spConsole);
        //splitPaneVer.setOneTouchExpandable(true);

        frame.getContentPane().add(BorderLayout.SOUTH, pButtons);
        frame.getContentPane().add(BorderLayout.CENTER, splitPaneHor);//splitPaneVer);
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int)(screenSize.height * 0.618);
        frame.setBounds(80, 30, (int)(height*1.618), height);
        //splitPaneVer.setDividerLocation((int)(splitPaneVer.getHeight()*0.618));
        //splitPaneVer.setDividerLocation(height - 200);

        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //close();
            }
        });

        show.put("bels", true);
        show.put("rules", false);
        show.put("evt", true);
        show.put("mb", false);
        show.put("int", false);
        show.put("int-details", false);
        show.put("plan", false);
        show.put("plan-details", false);
        show.put("annots", Config.get().getBoolean(Config.SHOW_ANNOTS));
    }

    @SuppressWarnings("unchecked")
    void setListOfAgsFromInfra() {
        try {
            Set<String> ags = new TreeSet<String>(getExecutionControlInfraTier().getRuntimeServices().getAgentsNames());
            for (String ag: ags) { //getExecutionControlInfraTier().getRuntimeServices().getAgentsNames()) {
                listModel.addElement(ag);
            }
        } catch (Exception e) {
            System.err.println("Error getting list of agents from infrasructure. "+e);
        }
    }

    private void setupSlider() {
        int size = getCycleNumber();
        Hashtable<Integer,Component> labelTable = new Hashtable<Integer,Component>();
        labelTable.put( 0, new JLabel("Cycle 0") );
        labelTable.put( size, new JLabel("Cycle "+size) );
        jHistory.setLabelTable( labelTable );
        jHistory.setMaximum(size);
        jHistory.setValue(size);
    }

    public void stop() {
        super.stop();
        frame.dispose();
        frame = null;
    }

    private void inspectAgent(String agName) {
        if (agName == null) {
            return;
        }
        if (agName.length() == 0) {
            return;
        }
        currentAgName = agName;

        agState = null;

        // try to get the state from the history
        try {
            agState = agsHistory.get(currentAgName).get(getCycleNumber());
        } catch (Exception et) {}

        // if no state from history, ask the agent
        if (agState == null) {
            try {
                // try to get from history
                agState = infraControl.getAgState(currentAgName);
                storeAgHistory(currentAgName, getCycleNumber(), agState);
            } catch (Exception e) {
                jTA.setText("Error getting the state of agent "+currentAgName);
                logger.log(Level.SEVERE,"Error:",e);
            }
        }

        showAgState();
        setupSlider();
    }

    private String previousMind = "--";

    /** show current agent state */
    void showAgState() {
        if (agState != null) {
            String sMind = null;
            try {
                // set parameters
                if (jCbViewAs.getSelectedItem().toString().equals("html")) {
                    // as HTML
                    for (String p: show.keySet()) {
                        agTransformer.getTransformer().setParameter("show-"+p, show.get(p)+"");
                    }
                }
                sMind = agTransformer.transform(agState);

                if (!sMind.equals(previousMind)) {
                    jTA.setText(sMind);
                }
                previousMind = sMind;
            } catch (Exception e) {
                jTA.setText("Error in XML transformation!" + e + "\nText="+sMind);
                e.printStackTrace();
            }
        }
    }


    private void hyperLink(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            //System.out.println("*evt="+evt.getDescription());
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
            showAgState();
        }
    }



    protected void startRun() {
        jBtRun.setEnabled(false);
        countCycles = 0;
        maxCycles = Integer.parseInt(jTfSteps.getText());
        breakpoint = false;
        if (jCbWho.getSelectedIndex() == 0) {
            waitAllAgs     = true;
            waitSelectedAg = false;
        } else {
            waitAllAgs     = false;
            waitSelectedAg = true;
        }
        if (BaseLocalMAS.getRunner() != null && BaseLocalMAS.getRunner().hasDebugControl()) {
            BaseLocalMAS.getRunner().enableDebugControl();
        }

        startNewCycle();
        continueRun();
    }

    /** stop the execution */
    protected void stopRun() {
        waitAllAgs     = false;
        waitSelectedAg = false;
        inspectAgent(currentAgName);
        jBtRun.setEnabled(true);
        setRunningCycle(false);
    }

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    protected void continueRun() {
        executor.execute(new Runnable() {
            public void run() {
                if (waitAllAgs) {
                    infraControl.informAllAgsToPerformCycle(getCycleNumber());
                }
                if (waitSelectedAg) {
                    infraControl.informAgToPerformCycle(currentAgName, getCycleNumber());
                }
            }
        });
    }

    /**
     * Called when the agent <i>agName</i> has finished its reasoning cycle.
     * <i>breakpoint</i> is true in case the agent selected one plan with "breakpoint"
     * annotation.
      */
    @SuppressWarnings("unchecked")
    public void receiveFinishedCycle(final String agName, boolean breakpoint, final int cycle) {
        if (!listModel.contains(agName)) {
            logger.fine("New agent "+agName);
            listModel.addElement(agName);
        }

        // get the state of this agent and add it in history
        try {
            storeAgHistory(agName, cycle, infraControl.getAgState(agName));
        } catch (Exception e) {
            jTA.setText("Error getting the state of agent "+agName);
            logger.log(Level.SEVERE,"Error:",e);
        }

        this.breakpoint = breakpoint;
        if (waitSelectedAg) {
            countCycles++;
            logger.info("Agent "+agName+" has finished cycle "+cycle);
            if (testStop()) {
                stopRun();
            } else {
                startNewCycle();
                continueRun();
            }
        } else {
            super.receiveFinishedCycle(agName, breakpoint, cycle);
        }
    }


    /** called when all agents have finished the current cycle */
    protected void allAgsFinished() {
        if (waitAllAgs) {
            logger.fine("All agents have finished cycle "+getCycleNumber());
            countCycles++;
            if (testStop()) {
                stopRun();
            } else {
                startNewCycle();
                frame.setTitle(title + " cycle "+getCycleNumber()+" ::");
                continueRun();
            }
        }
    }

    /** test whether to stop running and show the agent state */
    protected boolean testStop() {
        return breakpoint || countCycles >= maxCycles;
    }

    protected void storeAgHistory(String agName, int cycle, Document doc) {
        if (doc == null) return;
        Map<Integer,Document> lag = agsHistory.get(agName);
        if (lag == null) {
            lag = new HashMap<Integer,Document>();
            agsHistory.put(agName, lag);
        }
        lag.put(cycle,doc);
    }
}
