import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

/**
 * Simple Vacuum cleaning environment
 *
 * @author Jomi
 *
 */
public class VCWorld extends Environment {

    /** world model */
    private boolean[][] dirty =
      { { true, true },    // all dirty
        { true, true }
      };

    private int vcx = 0; // the vacuum cleaner location
    private int vcy = 0;

    private Object modelLock = new Object();

    /** general delegations */
    private HouseGUI gui = new HouseGUI();
    private Logger   logger = Logger.getLogger("env."+VCWorld.class.getName());
    private Random   r = new Random();

    /** constant terms used for perception */
    private static final Literal lPos1  = ASSyntax.createLiteral("pos", ASSyntax.createNumber(1));
    private static final Literal lPos2  = ASSyntax.createLiteral("pos", ASSyntax.createNumber(2));
    private static final Literal lPos3  = ASSyntax.createLiteral("pos", ASSyntax.createNumber(3));
    private static final Literal lPos4  = ASSyntax.createLiteral("pos", ASSyntax.createNumber(4));
    private static final Literal lDirty = ASSyntax.createLiteral("dirty");
    private static final Literal lClean = ASSyntax.createLiteral("clean");

    public VCWorld() {
        createPercept();

        // create a thread to add dirty
        new Thread() {
            public void run() {
                try {
                    while (isRunning()) {
                        // add random dirty
                        if (r.nextInt(100) < 20) {
                            dirty[r.nextInt(2)][r.nextInt(2)] = true;
                            gui.paint();
                            createPercept();
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {}
            }
        } .start();
    }

    /** create the agents perceptions based on the world model */
    private void createPercept() {
        // remove previous perception
        clearPercepts();

        if (vcx == 0 && vcy == 0) {
            addPercept(lPos1);
        } else if (vcx == 1 && vcy == 0) {
            addPercept(lPos2);
        } else if (vcx == 0 && vcy == 1) {
            addPercept(lPos3);
        } else if (vcx == 1 && vcy == 1) {
            addPercept(lPos4);
        }

        if (dirty[vcx][vcy]) {
            addPercept(lDirty);
        } else {
            addPercept(lClean);
        }
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info("doing "+action);

        try {
            Thread.sleep(500);   // slow down the execution
        }  catch (Exception e) {}

        synchronized (modelLock) {
            // Change the world model based on action
            if (action.getFunctor().equals("suck")) {
                if (dirty[vcx][vcy]) {
                    dirty[vcx][vcy] = false;
                } else {
                    logger.info("suck in a clean location!");
                    Toolkit.getDefaultToolkit().beep();
                }
            } else if (action.getFunctor().equals("left")) {
                if (vcx > 0) {
                    vcx--;
                }
            } else if (action.getFunctor().equals("right")) {
                if (vcx < 1) {
                    vcx++;
                }
            } else if (action.getFunctor().equals("up")) {
                if (vcy > 0) {
                    vcy--;
                }
            } else if (action.getFunctor().equals("down")) {
                if (vcy < 1) {
                    vcy++;
                }
            } else {
                logger.info("The action "+action+" is not implemented!");
                return false;
            }
        }

        createPercept(); // update agents perception for the new world state
        gui.paint();
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        gui.setVisible(false);
    }


    /* a simple GUI */
    class HouseGUI extends JFrame {
        JLabel[][] labels;

        HouseGUI() {
            super("Domestic Robot");
            labels = new JLabel[dirty.length][dirty.length];
            getContentPane().setLayout(new GridLayout(labels.length, labels.length));
            for (int j = 0; j < labels.length; j++) {
                for (int i = 0; i < labels.length; i++) {
                    labels[i][j] = new JLabel();
                    labels[i][j].setPreferredSize(new Dimension(180,180));
                    labels[i][j].setHorizontalAlignment(JLabel.CENTER);
                    labels[i][j].setBorder(new EtchedBorder());
                    getContentPane().add(labels[i][j]);
                }
            }
            pack();
            setVisible(true);
            paint();
        }

        void paint() {
            synchronized (modelLock) { // do not allow changes in the robot location while painting
                for (int i = 0; i < labels.length; i++) {
                    for (int j = 0; j < labels.length; j++) {
                        String l = "<html><center>";
                        if (vcx == i && vcy == j) {
                            l += "<font color=\"red\" size=7><b>Robot</b><br></font>";
                        }
                        if (dirty[i][j]) {
                            l += "<font color=\"blue\" size=6>*kaka*</font>";
                        }
                        l += "</center></html>";
                        labels[i][j].setText(l);
                    }
                }
            }
        }
    }
}

