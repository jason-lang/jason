package jason.bb;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;

/**
 * Implementation of BB that stores the agent BB in text files. This
 * implementation is very simple: when the agent starts, load the
 * beliefs in the file; when the agent stops, save the BB in the file.
 * The file name is the agent's name + ".bb".
 */
public class TextPersistentBB extends ChainBBAdapter {
    private static Logger logger = Logger.getLogger(TextPersistentBB.class.getName());

    private File file = null;

    public TextPersistentBB() { }
    public TextPersistentBB(BeliefBase next) {
        super(next);
    }

    public void init(Agent ag, String[] args) {
        if (ag != null) {
            try {
                file = new File(ag.getTS().getUserAgArch().getAgName() + ".bb");
                logger.fine("reading from file " + file);
                if (file.exists()) {
                    ag.parseAS(file.getAbsoluteFile());
                    ag.addInitialBelsInBB();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error initialising TextPersistentBB.",e);
            }
        }
    }

    public void stop() {
        try {
            logger.fine("writting to file " + file);
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.println("// BB stored by TextPersistentBB\n");
            for (Literal b: this) {
                if (! b.isRule()) {
                    out.println(b.toString()+".");
                }
            }
            out.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing BB in file " + file, e);
        }
        nextBB.stop();
    }
}
