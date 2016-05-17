package mds;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.*;

/** example of agent function overriding */
public class MDSAgent extends Agent {

    private Trigger unattendedLuggage = Trigger.parseTrigger("+unattended_luggage(_,_,_)");
    
    /** unattended_luggage event has priority */
    @Override
    public Event selectEvent(Queue<Event> events) {
        Iterator<Event> i = events.iterator();
        while (i.hasNext()) {
            Event e = i.next();
            if (e.getTrigger().getLiteral().getFunctor().equals("unattended_luggage")) {
                i.remove();
                return e;
            }

            // the intention that handles +unattended_luggage could have a
            // sub-goal that generates other events (+!<sub-goal>), in this case
            // the +unattended_luggage event is in the bottom of the stack
            if (e.getIntention() != null) {
                for (IntendedMeans im: e.getIntention()) {
                    if (im.getPlan().getTrigger().getLiteral().getFunctor().equals("unattended_luggage")) {
                        i.remove();
                        return e;
                    }
                }
            }
        }
        return super.selectEvent(events);
    }
    
    /** prefer to select intentions with +unattended_luggage(_,_,_) event in stack of IMs */
    @Override
    public Intention selectIntention(Queue<Intention> intentions) {
        if (intentions.size() > 1) { // only search for a particular intention if there exists more options
            Iterator<Intention> i = intentions.iterator();
            while (i.hasNext()) {
                Intention cit = i.next();
                if (cit.hasTrigger(unattendedLuggage, new Unifier())) {
                    i.remove();
                    return cit;
                }
            }
        }
        
        // do not find +unattended_luggage(_,_,_), use default selection
        return super.selectIntention(intentions);
    }
}
