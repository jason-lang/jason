package jason.asSemantics;

import jason.asSyntax.Plan;
import jason.util.ToDOM;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An Option is a Plan and the Unifier that has made it relevant and applicable
 */

public class Option implements Serializable, ToDOM {

    private static final long serialVersionUID = 1L;

    private Plan    plan;
    private Unifier unif;

    private Event   evt; // the event this is an option for

    public Option(Plan p, Unifier u) {
        plan = p;
        unif = u;
    }

    public Option(Plan p, Unifier u, Event e) {
        plan = p;
        unif = u;
        evt  = e;
    }

    public Object clone() {
        return new Option((Plan) plan.clone(), (Unifier) unif.clone(), (Event)evt.clone());
    }

    public String toString() {
        return "(" + plan + "," + unif + ")";
    }

    public void setPlan(Plan p) {
        plan = p;
    }
    public Plan getPlan() {
        return plan;
    }

    public void setUnifier(Unifier u) {
        unif = u;
    }
    public Unifier getUnifier() {
        return unif;
    }

    public Event getEvt() { return evt; }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element op = (Element) document.createElement("option");
        if (plan != null) {
            op.appendChild(plan.getAsDOM(document));
        }
        if (unif != null) {
            op.appendChild(unif.getAsDOM(document));
        }
        return op;
    }

}
