// Internal action code for project FoodSimulation

package bb;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.logging.*;

/** list all BB */
public class list extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("FoodSimulation."+list.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            String s = "  .";
            for (Literal l: ts.getAg().getBB()) {
                s += l + " ";
            }
            logger.info(s);
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'bb.list'! "+e);
        }
        return false;
    }
}
