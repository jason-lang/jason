
package agent;

import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Unifier;
import jason.asSyntax.Trigger;

import java.util.Iterator;
import java.util.Queue;

/**
 * change the default select event function to prefer cell(_,_,gold) events.
 *
 * @author Jomi
 */
public class SelectEvent extends Agent {

    private Trigger gold    = Trigger.parseTrigger("+cell(_,_,gold)");
    private Trigger restart = Trigger.parseTrigger("+restart");
    private Unifier un   = new Unifier();

    public Event selectEvent(Queue<Event> events) {
        Iterator<Event> ie = events.iterator();
        while (ie.hasNext()) {
            un.clear();
            Event e = ie.next();
            if (un.unifies(gold, e.getTrigger()) || un.unifies(restart, e.getTrigger())) {
                //getTS().getLogger().info("custom select event "+e);
                ie.remove();
                return e;
            }
        }
        return super.selectEvent(events);
    }


}
