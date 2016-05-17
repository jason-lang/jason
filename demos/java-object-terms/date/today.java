// Internal action code for project ObjectTerm

package date;

import jason.asSemantics.*;
import jason.asSyntax.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

public class today extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("ObjectTerm."+today.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            // create the ObjectTerm (the argument is any java object)
            Term t = new ObjectTermImpl(new NiceToStringForCalendar());
            
            // unifies the new term with the arguments of the internal action
            return un.unifies(args[0], t);
        } catch (Exception e) {
            logger.warning("Error in internal action 'date.today'! "+e);
        }
        return false;
    }
    
    // a class to printout dates
    class NiceToStringForCalendar extends GregorianCalendar {
        @Override
        public String toString() {
            return  (get(Calendar.MONTH)+1) + "/" + get(Calendar.DAY_OF_MONTH) + "/" + get(Calendar.YEAR);
        }
    }

}
