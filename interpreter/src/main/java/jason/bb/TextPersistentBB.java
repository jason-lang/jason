package jason.bb;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSemantics.Agent;
import jason.asSyntax.Atom;
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
    private Agent ag  = null;

    public TextPersistentBB() { }
    public TextPersistentBB(BeliefBase next) {
        super(next);
    }

    @Override
    public void init(Agent ag, String[] args) {
        this.ag = ag;
        if (ag != null) {
            try {
                file = new File(ag.getTS().getAgArch().getAgName() + ".bb");
                logger.info("reading BB from file " + file);
                if (file.exists()) {
                    ag.parseAS(file.getAbsoluteFile());
                    ag.addInitialBelsInBB();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error initialising TextPersistentBB.",e);
            }
        }
    }

    @Override
    public void stop() {
        try {
            logger.info("storing BB in file " + file);
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.println("// BB stored by TextPersistentBB for agent '"+ag.getTS().getAgArch().getAgName()+"'\n");
            for (Literal b: this) {
                if (! b.isRule() && !b.hasAnnot(BeliefBase.TPercept) && !b.getNS().equals(new Atom("kqml"))) {
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
