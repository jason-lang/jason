package gui;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.logging.*;

import javax.swing.JOptionPane;

public class yes_no extends ConcurrentInternalAction {

    private Logger logger = Logger.getLogger("gui."+yes_no.class.getName());

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, final Term[] args) throws Exception {
        try {
            // suspend the intention (max 5 seconds)
            final String key = suspendInt(ts, "gui", 5000); 

            // to not block the agent thread, 
            // create a new task that show the GUI and resume the intention latter 
            startInternalAction(ts, new Runnable() {
                public void run() {
                    int answer = JOptionPane.showConfirmDialog(null, args[0].toString());
                    
                    if (answer == JOptionPane.YES_OPTION)
                        resumeInt(ts, key); // resume the intention with success
                    else
                        failInt(ts, key); // resume the intention with fail
                }
            });
            
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'gui.yes_no'! "+e);
        }
        return false;
    }
    
    /** called back when some intention should be resumed/failed by timeout */
    @Override
    public void timeout(TransitionSystem ts, String intentionKey) {
        // this method have to decide what to do with actions finished by timeout
        // 1: resume
        //resumeInt(ts,intentionKey);
        
        // 2: fail
        failInt(ts, intentionKey);
    }
}
