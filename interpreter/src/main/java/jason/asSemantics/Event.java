package jason.asSemantics;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.Trigger;
import jason.util.ToDOM;

public class Event implements Serializable, ToDOM {

    private static final long serialVersionUID = 1L;

    Trigger   trigger   = null;
    Intention intention = Intention.EmptyInt;
    Option    option    = null; // option computed in selEv (JasonER)

    public Event(Trigger t, Intention i) {
        trigger   = t;
        intention = i;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Intention getIntention() {
        return intention;
    }
    public void setIntention(Intention i) {
        intention = i;
    }

    public void setOption(Option po) {
        option = po;
    }
    public Option getOption() {
        return option;
    }

    public boolean sameTE(Object t) {
        return trigger.equals(t);
    }

    public boolean isExternal() {
        return intention == Intention.EmptyInt;
    }
    public boolean isInternal() {
        return intention != Intention.EmptyInt;
    }
    public boolean isAtomic() {
        return intention != null && intention.isAtomic();
    }

    @Override
    public int hashCode() {
        int r = trigger.hashCode();
        if (intention != null)
            r = r + intention.hashCode();
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Event oe) {
            if (this.intention == null && oe.intention != null) return false;
            if (this.intention != null && !this.intention.equals(oe.intention)) return false;

            return this.trigger.equals(oe.trigger);
        }
        return false;
    }

    public Object clone() {
        Trigger   tc = (trigger   == null ? null : (Trigger)trigger.clone());
        Intention ic = (intention == null ? null : (Intention)intention.clone());
        return new Event(tc, ic);
    }

    public String toString() {
        if (intention == Intention.EmptyInt)
            return ""+trigger;
        else
            return trigger+"\n"+intention;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element eevt = (Element) document.createElement("event");
        eevt.appendChild(trigger.getAsDOM(document));
        if (intention != Intention.EmptyInt) {
            eevt.setAttribute("intention", intention.getId()+"");
        }
        return eevt;
    }

}
