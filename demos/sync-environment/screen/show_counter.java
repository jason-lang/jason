// Internal action code for project act-sync.mas2j

package screen;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

import java.util.logging.Logger;

public class show_counter extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("act-sync.mas2j."+show_counter.class.getName());

    private int pos = -1;
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            if (pos < 0) {
                pos = Counters.get().getPlace(ts.getUserAgArch().getAgName());
                //logger.info("position of "+ts.getUserAgArch().getAgName()+" is "+pos);
            }
            Counters.get().setVl(pos, (int)((NumberTerm)args[0]).solve());
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'show_counter'! "+e);
        }
        return false;
    }
}

