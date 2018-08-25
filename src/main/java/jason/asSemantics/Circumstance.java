package jason.asSemantics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.Literal;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.infra.centralised.CentralisedAgArch;

public class Circumstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private   Queue<Event>             E;
    private   Queue<Intention>         I;
    protected ActionExec               A;
    private   Queue<Message>           MB;
    protected List<Option>             RP;
    protected List<Option>             AP;
    protected Event                    SE;
    protected Option                   SO;
    protected Intention                SI;
    private   Intention                AI; // Atomic Intention
    private   Event                    AE; // Atomic Event
    private   boolean                  atomicIntSuspended = false; // whether the current atomic intention is suspended in PA or PI
    //private   boolean                  hasAtomicEvent = false;

    private Map<Integer, ActionExec>   PA; // Pending actions, waiting action execution (key is the intention id)
    private List<ActionExec>           FA; // Feedback actions, those that are already executed

    private Map<String, Intention>     PI; // pending intentions, intentions suspended by any other reason
    private Map<String, Event>         PE; // pending events, events suspended by .suspend

    private Queue<CircumstanceListener> listeners = new ConcurrentLinkedQueue<CircumstanceListener>();

    private TransitionSystem ts = null;

    public Object syncApPlanSense = new Object();

    public Circumstance() {
        create();
        reset();
    }

    public void setTS(TransitionSystem ts) {
        this.ts = ts;
    }

    /** creates new collections for E, I, MB, PA, PI, and FA */
    public void create() {
        // use LinkedList since we use a lot of remove(0) in selectEvent
        E  = new ConcurrentLinkedQueue<Event>();
        I  = new ConcurrentLinkedQueue<Intention>();
        MB = new ConcurrentLinkedQueue<Message>();
        PA = new ConcurrentHashMap<Integer, ActionExec>();
        PI = new ConcurrentHashMap<String, Intention>();
        PE = new ConcurrentHashMap<String, Event>();
        FA = new ArrayList<ActionExec>();
    }

    /** set null for A, RP, AP, SE, SO, and SI */
    public void reset() {
        resetSense();
        resetDeliberate();
        resetAct();
    }

    public void resetSense() {
    }

    public void resetDeliberate() {
        RP = null;
        AP = null;
        SE = null;
        SO = null;
    }

    public void resetAct() {
        A  = null;
        SI = null;
    }

    public Event addAchvGoal(Literal l, Intention i) {
        Event evt = new Event(new Trigger(TEOperator.add, TEType.achieve, l), i);
        addEvent(evt);
        return evt;
    }

    public void addExternalEv(Trigger trig) {
        addEvent(new Event(trig, Intention.EmptyInt));
    }

    /** Events */

    public void addEvent(Event ev) {

        if (ev.isAtomic())
            AE = ev;
        else
            E.add(ev);

        // notify listeners
        if (listeners != null)
            for (CircumstanceListener el : listeners)
                el.eventAdded(ev);
    }

    public void insertMetaEvent(Event ev) {
        // meta events have to be placed in the begin of the queue, but not before other meta events
        List<Event> newE = new ArrayList<Event>(E); // make a list of events to find the best place to insert the new event
        int pos = 0;
        for (Event e: newE) {
            if (!e.getTrigger().isMetaEvent()) {
                break;
            }
            pos++;
        }
        newE.add(pos,ev);
        E.clear();
        E.addAll(newE);

        // notify listeners
        if (listeners != null)
            for (CircumstanceListener el : listeners)
                el.eventAdded(ev);
    }

    public boolean removeEvent(Event ev) {
        boolean removed = false;
        if (ev.equals(AE)) {
            AE = null;
            removed = true;
        } else {
            removed = E.remove(ev);
        }
        if (removed && ev.getIntention() != null && listeners != null)
            for (CircumstanceListener el : listeners)
                el.intentionDropped(ev.getIntention());
        return removed;
    }

    // remove events based on a match with a trigger
    public void removeEvents(Trigger te, Unifier un) {
        Iterator<Event> ie = E.iterator();
        while (ie.hasNext()) {
            Event ev = ie.next();
            Trigger t = ev.getTrigger();
            if (ev.getIntention() != Intention.EmptyInt) { // since the unifier of the intention will not be used, apply it to the event before comparing to the event to be dropped
                t = t.capply(ev.getIntention().peek().getUnif());
            }
            if (un.clone().unifiesNoUndo(te, t)) {
                ie.remove();
                if (ev.getIntention() != null && listeners != null)
                    for (CircumstanceListener el : listeners)
                        el.intentionDropped(ev.getIntention());
            }
        }
    }

    public void clearEvents() {
        // notify listeners
        if (listeners != null)
            for (CircumstanceListener el : listeners) {
                for (Event ev: E)
                    if (ev.getIntention() != null)
                        el.intentionDropped(ev.getIntention());
                if (AE != null && AE.getIntention() != null)
                    el.intentionDropped(AE.getIntention());
            }

        E.clear();
        AE = null;
    }

    /** get the queue of events (which does not include the atomic event) */
    public Queue<Event> getEvents() {
        return E;
    }

    /** get the all events (which include the atomic event, if it exists) */
    public Iterator<Event> getEventsPlusAtomic() {
        if (AE == null) {
            return E.iterator();
        } else {
            List<Event> l = new ArrayList<Event>(E.size()+1);
            l.add(AE);
            l.addAll(E);
            return l.iterator();
        }
    }

    public boolean hasEvent() {
        return AE != null || !E.isEmpty();
    }

    public Event getAtomicEvent() {
        return AE;
    }

    /** remove and returns the event with atomic intention, null if none */
    public Event removeAtomicEvent() {
        Event e = AE;
        AE = null;
        if (e != null && e.getIntention() != null && listeners != null)
            for (CircumstanceListener el : listeners)
                el.intentionDropped(e.getIntention());

        return e;
        /*if (!hasAtomicEvent)
            return null;

        Iterator<Event> i = E.iterator();
        while (i.hasNext()) {
            Event e = i.next();
            if (e.isAtomic()) {
                i.remove();
                hasAtomicEvent = false;
                return e;
            }
        }
        // there is no AtomicEvent!
        hasAtomicEvent = false;
        return null;*/
    }

    /** Listeners */

    public void addEventListener(CircumstanceListener el) {
        //if (listeners == null)
        //    listeners = new CopyOnWriteArrayList<CircumstanceListener>();
        listeners.add(el);
    }

    public void removeEventListener(CircumstanceListener el) {
        if (el != null) {
            listeners.remove(el);
            //if (listeners.isEmpty())
            //    listeners = null;
        }
    }

    public boolean hasListener() {
        return !listeners.isEmpty();
    }

    public Collection<CircumstanceListener> getListeners() {
        return listeners;
    }

    /** Messages */

    public Queue<Message> getMailBox() {
        return MB;
    }

    public void addMsg(Message m) {
        MB.offer(m);
    }

    public boolean hasMsg() {
        return !MB.isEmpty();
    }

    /** Intentions */

    /** get the queue of intention (which does not include atomic intention) */
    public Queue<Intention> getIntentions() {
        return I;
    }

    /** get the all intentions (which include the atomic intention, if it exists) */
    public Iterator<Intention> getIntentionsPlusAtomic() {
        if (AI == null) {
            return I.iterator();
        } else {
            List<Intention> l = new ArrayList<Intention>(I.size()+1);
            l.add(AI);
            l.addAll(I);
            return l.iterator();
        }
    }

    public boolean hasIntention() {
        return (I != null && !I.isEmpty()) || AI != null;
    }
    public boolean hasIntention(Intention i) {
        return i == AI || I.contains(i);
    }

    public void addIntention(Intention intention) {
        if (intention.isAtomic())
            setAtomicIntention(intention);
        else
            I.offer(intention);

        // notify
        if (listeners != null)
            for (CircumstanceListener el : listeners)
                el.intentionAdded(intention);
    }

    /** add the intention back to I, and also notify meta listeners that the goals are resumed  */
    public void resumeIntention(Intention intention) {
        addIntention(intention);

        // notify meta event listeners
        if (listeners != null)
            for (CircumstanceListener el : listeners)
                el.intentionResumed(intention);
    }

    /** remove intention from set I */
    public boolean removeIntention(Intention i) {
        if (i == AI) {
            setAtomicIntention(null);
            return true;
        } else {
            return I.remove(i);
        }
    }

    /** removes and produces events to signal that the intention was dropped */
    public boolean dropIntention(Intention i) {
        if (removeIntention(i)) {
            if (listeners != null)
                for (CircumstanceListener el : listeners)
                    el.intentionDropped(i);
            return true;
        } else {
            return false;
        }
    }

    public void clearIntentions() {
        setAtomicIntention(null);

        if (listeners != null)
            for (CircumstanceListener el : listeners)
                for (Intention i: I)
                    el.intentionDropped(i);

        I.clear();
    }

    public void setAtomicIntention(Intention i) {
        AI = i;
    }

    public Intention removeAtomicIntention() {
        if (AI != null) {
            if (atomicIntSuspended) {
                //throw new JasonException("Internal error: trying to remove the atomic intention, but it is suspended! it should be removed only when back to I!");
                return null;
            }
            Intention tmp = AI;
            removeIntention(AI);
            return tmp;
        }
        return null;
    }

    public boolean hasAtomicIntention() {
        //String x = (AI != null ? ""+AI.size() : "");
        //System.out.println(E.size()+" "+I.size()+" "+(AI != null)+" "+(AE != null)+" "+ x);
        return AI != null;
    }

    public boolean isAtomicIntentionSuspended() {
        return AI != null && atomicIntSuspended;
    }

    /** pending intentions */

    public Map<String, Intention> getPendingIntentions() {
        return PI;
    }

    public boolean hasPendingIntention() {
        return PI != null && !PI.isEmpty();
    }

    public void clearPendingIntentions() {
        // notify listeners
        if (listeners != null)
            for (CircumstanceListener el : listeners)
                for (Intention i: PI.values())
                    el.intentionDropped(i);

        PI.clear();
    }

    public void addPendingIntention(String id, Intention i) {
        if (i.isAtomic()) {
            setAtomicIntention(i);
            atomicIntSuspended = true;
        }
        PI.put(id, i);

        if (listeners != null)
            for (CircumstanceListener el : listeners)
                el.intentionSuspended(i, id);
    }

    public Intention removePendingIntention(String pendingId) {
        Intention i = PI.remove(pendingId);
        if (i != null && i.isAtomic()) {
            atomicIntSuspended = false;
        }
        return i;
    }
    public Intention removePendingIntention(int intentionId) {
        for (String key: PI.keySet()) {
            Intention pii = PI.get(key);
            if (pii.getId() == intentionId)
                return removePendingIntention(key);
        }
        return null;
    }

    /** removes the intention i from PI and notify listeners that the intention was dropped */
    public boolean dropPendingIntention(Intention i) {
        // use a loop instead of get because the intention (the value) is used in the search instead of the key
        for (String key: PI.keySet()) {
            Intention pii = PI.get(key);
            if (pii.equals(i)) {
                removePendingIntention(key);

                // check in wait internal action
                if (listeners != null)
                    for (CircumstanceListener el : listeners)
                        el.intentionDropped(i);
                return true;
            }
        }
        return false;
    }

    /** pending events */

    public Map<String, Event> getPendingEvents() {
        return PE;
    }

    public boolean hasPendingEvent() {
        return PE != null && PE.size() > 0;
    }

    public void clearPendingEvents() {
        // notify listeners
        if (listeners != null)
            for (CircumstanceListener el : listeners)
                for (Event e: PE.values())
                    if (e.getIntention() != null)
                        el.intentionDropped(e.getIntention());

        PE.clear();
    }

    public void addPendingEvent(String id, Event e) {
        PE.put(id, e);

        if (listeners != null && e.getIntention() != null)
            for (CircumstanceListener el : listeners)
                el.intentionSuspended(e.getIntention(), id);
    }

    public Event removePendingEvent(String pendingId) {
        Event e = PE.remove(pendingId);
        if (e != null && listeners != null && e.getIntention() != null)
            for (CircumstanceListener el : listeners)
                el.intentionDropped(e.getIntention());
        return e;
    }

    public void removePendingEvents(Trigger te, Unifier un) {
        Iterator<Event> ie = PE.values().iterator();
        while (ie.hasNext()) {
            Event  ev = ie.next();
            Trigger t = ev.getTrigger();
            if (ev.getIntention() != Intention.EmptyInt) { // since the unifier of the intention will not be used, apply it to the event before comparing to the event to be dropped
                t = t.capply(ev.getIntention().peek().getUnif());
            }
            if (un.clone().unifiesNoUndo(te, t)) {
                ie.remove();

                if (listeners != null && ev.getIntention() != null)
                    for (CircumstanceListener el : listeners)
                        el.intentionDropped(ev.getIntention());
            }
        }

    }

    /** actions */

    public ActionExec getAction() {
        return A;
    }

    public void setAction(ActionExec a) {
        this.A = a;
    }

    public List<Option> getApplicablePlans() {
        return AP;
    }

    /** feedback action */

    public boolean hasFeedbackAction() {
        return !FA.isEmpty();
    }

    public List<ActionExec> getFeedbackActions() {
        return FA;
    }
    /*
    public List<ActionExec> getFeedbackActionsWrapper() {
        return new AbstractList<ActionExec>() {
            public boolean add(ActionExec act) {
                addFeedbackAction(act);
                return true;
            }
            public int size()                { return 0; }
            public ActionExec get(int index) { return null; }
        };
    }
    */

    public void addFeedbackAction(ActionExec act) {
        if (act.getIntention() != null) {
            synchronized (FA) {
                FA.add(act);
                /*if (act.getIntention().isAtomic()) {
                    ts.getLogger().info("feedback atomic "+act.getIntention().getId());
                    //atomicIntSuspended = false; // TODO: here is the bug (reported by Olivier @ ALTISSIMO)
                }*/
            }
        }
    }


    /** pending action */

    public Map<Integer, ActionExec> getPendingActions() {
        return PA;
    }

    public void addPendingAction(ActionExec a) {
        Intention i = a.getIntention();
        if (i.isAtomic()) {
            setAtomicIntention(i);
            atomicIntSuspended = true;
        }
        PA.put(i.getId(), a);

        if (listeners != null)
            for (CircumstanceListener el : listeners)
                el.intentionSuspended(i, "action "+a.getActionTerm());
    }

    public void clearPendingActions() {
        // notify listeners
        if (listeners != null)
            for (CircumstanceListener el : listeners)
                for (ActionExec act: PA.values())
                    el.intentionDropped(act.getIntention());

        PA.clear();
    }

    public boolean hasPendingAction() {
        return PA != null && !PA.isEmpty();
    }


    public ActionExec removePendingAction(int intentionId) {
        ActionExec a = PA.remove(intentionId);
        if (a != null && a.getIntention().isAtomic()) {
            atomicIntSuspended = false;
        }
        return a;
    }

    /** removes the intention i from PA and notify listeners that the intention was dropped */
    public boolean dropPendingAction(Intention i) {
        ActionExec act = removePendingAction(i.getId());
        if (act != null) {
            // check in wait internal action
            if (listeners != null)
                for (CircumstanceListener el : listeners)
                    el.intentionDropped(i);

            return true;
        }
        /*
        Iterator<ActionExec> it = PA.values().iterator();
        while (it.hasNext()) {
            if (it.next().getIntention().equals(i)) {
                removePendingAction(i.getId());

                // check in wait internal action
                for (CircumstanceListener el : listeners) {
                    el.intentionDropped(i);
                }
                return true;
            }
        }
        */
        return false;
    }

    public List<Option> getRelevantPlans() {
        return RP;
    }

    public Event getSelectedEvent() {
        return SE;
    }

    public Intention getSelectedIntention() {
        return SI;
    }

    public Option getSelectedOption() {
        return SO;
    }

    /** clone E, I, MB, PA, PI, FA, and AI */
    public Circumstance clone() {
        Circumstance c = new Circumstance();
        //c.hasAtomicEvent     = this.hasAtomicEvent;
        if (this.AE != null)
            c.AE             = (Event)this.AE.clone();
        c.atomicIntSuspended = this.atomicIntSuspended;

        for (Event e: this.E) {
            c.E.add((Event)e.clone());
        }
        for (Intention i: this.I) {
            c.I.add((Intention)i.clone());
        }
        for (Message m: this.MB) {
            c.MB.add((Message)m.clone());
        }
        for (int k: this.PA.keySet()) {
            c.PA.put(k, (ActionExec)PA.get(k).clone());
        }
        for (String k: this.PI.keySet()) {
            c.PI.put(k, (Intention)PI.get(k).clone());
        }
        for (String k: this.PE.keySet()) {
            c.PE.put(k, (Event)PE.get(k).clone());
        }
        for (ActionExec ae: FA) {
            c.FA.add((ActionExec)ae.clone());
        }
        return c;
    }


    /** get the agent circumstance as XML */
    public Element getAsDOM(Document document) {
        Element c = (Element) document.createElement("circumstance");
        Element e;
        boolean add;

        // MB
        add = false;
        Element ms = (Element) document.createElement("mailbox");
        if (MB != null && hasMsg()) {
            for (Message m: MB) {
                add = true;
                e = (Element) document.createElement("message");
                e.appendChild(document.createTextNode(m.toString()));
                ms.appendChild(e);
            }
        }
        if (ts != null) {
            try {
                for (Message m: ((CentralisedAgArch)ts.getUserAgArch()).getMBox()) {
                    add = true;
                    e = (Element) document.createElement("message");
                    e.appendChild(document.createTextNode(m.toString() + " in arch inbox."));
                    ms.appendChild(e);
                }

            } catch (Exception ex) { }
        }
        if (add)
            c.appendChild(ms);

        // events
        Element events = (Element) document.createElement("events");
        add = false;
        if (E != null && hasEvent()) {
            Iterator<Event> ie = getEventsPlusAtomic();
            while (ie.hasNext()) {
                Event evt = ie.next();

                add = true;
                e = evt.getAsDOM(document);
                events.appendChild(e);
            }
        }
        if (getSelectedEvent() != null) {
            add = true;
            e = getSelectedEvent().getAsDOM(document);
            e.setAttribute("selected", "true");
            events.appendChild(e);
        }
        if (hasPendingEvent()) {
            for (String k: PE.keySet()) {
                add = true;
                e = PE.get(k).getAsDOM(document);
                e.setAttribute("pending", k);
                events.appendChild(e);
            }
        }
        if (add) {
            c.appendChild(events);
        }

        // relPlans
        Element plans = (Element) document.createElement("options");
        List<Object> alreadyIn = new ArrayList<Object>();

        // option
        if (getSelectedOption() != null) {
            alreadyIn.add(getSelectedOption());
            e = getSelectedOption().getAsDOM(document);
            e.setAttribute("relevant", "true");
            e.setAttribute("applicable", "true");
            e.setAttribute("selected", "true");
            plans.appendChild(e);
        }

        // appPlans
        if (getApplicablePlans() != null && !getApplicablePlans().isEmpty()) {
            for (Option o : getApplicablePlans()) {
                if (!alreadyIn.contains(o)) {
                    alreadyIn.add(o);
                    e = o.getAsDOM(document);
                    e.setAttribute("relevant", "true");
                    e.setAttribute("applicable", "true");
                    plans.appendChild(e);
                }
            }
        }

        if (getRelevantPlans() != null && !getRelevantPlans().isEmpty()) {
            for (Option o: getRelevantPlans()) {
                if (!alreadyIn.contains(o)) {
                    alreadyIn.add(o);
                    e = o.getAsDOM(document);
                    e.setAttribute("relevant", "true");
                    plans.appendChild(e);
                }
            }
        }

        if (!alreadyIn.isEmpty()) {
            c.appendChild(plans);
        }

        // intentions
        Element ints = (Element) document.createElement("intentions");
        Element selIntEle = null;
        Intention ci = getSelectedIntention();
        if (ci != null) {
            selIntEle = ci.getAsDOM(document);
            selIntEle.setAttribute("selected", "true");
            ints.appendChild(selIntEle);
        }
        Iterator<Intention> itint = getIntentionsPlusAtomic();
        while (itint.hasNext()) {
            Intention in = itint.next();
            if (getSelectedIntention() != in) {
                ints.appendChild(in.getAsDOM(document));
            }
        }

        // pending intentions
        for (String wip : getPendingIntentions().keySet()) {
            Intention ip = getPendingIntentions().get(wip);
            if (getSelectedIntention() != ip) {
                e = ip.getAsDOM(document);
                e.setAttribute("pending", wip);
                ints.appendChild(e);
            }
        }
        if (hasPendingAction()) {
            for (int key : getPendingActions().keySet()) {
                ActionExec ac = getPendingActions().get(key);
                Intention aci = ac.getIntention();
                if (getSelectedIntention() != null && getSelectedIntention().equals(aci)) {
                    selIntEle.setAttribute("pending", ac.getActionTerm().toString());
                } else if (aci != null) {
                    e = aci.getAsDOM(document);
                    e.setAttribute("pending", ac.getActionTerm().toString());
                    ints.appendChild(e);
                }
            }
        }

        Element acts = (Element) document.createElement("actions");
        alreadyIn = new ArrayList<Object>();

        // action
        if (getAction() != null) {
            alreadyIn.add(getAction());
            e = getAction().getAsDOM(document);
            e.setAttribute("selected", "true");
            if (getPendingActions().values().contains(getAction())) {
                e.setAttribute("pending", "true");
            }
            synchronized (getFeedbackActions()) {
                if (getFeedbackActions().contains(getAction())) {
                    e.setAttribute("feedback", "true");
                }
            }
            acts.appendChild(e);
        }

        // pending actions
        if (hasPendingAction()) {
            for (int key : getPendingActions().keySet()) {// .iterator();
                ActionExec ac = getPendingActions().get(key);
                if (!alreadyIn.contains(ac)) {
                    e = ac.getAsDOM(document);
                    e.setAttribute("pending", key+"");
                    acts.appendChild(e);
                    alreadyIn.add(ac);
                }
            }
        }

        // FA
        if (hasFeedbackAction()) {
            for (ActionExec o: getFeedbackActions()) {
                if (!alreadyIn.contains(o)) {
                    alreadyIn.add(o);
                    e = o.getAsDOM(document);
                    e.setAttribute("feedback", "true");
                    acts.appendChild(e);
                }
            }
        }

        if (ints.getChildNodes().getLength() > 0) {
            c.appendChild(ints);
        }

        if (acts.getChildNodes().getLength() > 0) {
            c.appendChild(acts);
        }

        return c;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("Circumstance:\n");
        s.append("  E ="+E +"\n");
        s.append("  I ="+I +"\n");
        s.append("  A ="+A +"\n");
        s.append("  MB="+MB+"\n");
        s.append("  RP="+RP+"\n");
        s.append("  AP="+AP+"\n");
        s.append("  SE="+SE+"\n");
        s.append("  SO="+SO+"\n");
        s.append("  SI="+SI+"\n");
        s.append("  AI="+AI+"\n");
        s.append("  AE="+AE+"\n");
        s.append("  PA="+PA+"\n");
        s.append("  PI="+PI+"\n");
        s.append("  FA="+FA+".");
        return s.toString();
    }

}
