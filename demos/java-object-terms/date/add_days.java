// Internal action code for project ObjectTerm

package date;

import jason.asSemantics.*;
import jason.asSyntax.*;

import java.util.Calendar;
import java.util.logging.Logger;

public class add_days extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("ObjectTerm."+add_days.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            // get the object wrapped by args[0]
            Calendar c = (Calendar) ((ObjectTerm)args[0]).getObject();

            // clone (so to not change the original object)
            c = (Calendar)c.clone();

            // do the changes
            c.add(Calendar.DAY_OF_YEAR, (int)((NumberTerm)args[1]).solve());

            // unify the result
            return un.unifies(args[2], new ObjectTermImpl(c));
        } catch (Exception e) {
            logger.warning("Error in internal action 'date.add_days'! "+e);
        }
        return false;
    }
}
