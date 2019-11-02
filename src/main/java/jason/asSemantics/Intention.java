package jason.asSemantics;

import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.util.Pair;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents and Intention (a stack of IntendedMeans).
 *
 * The comparable sorts the intentions based on the atomic property:
 * atomic intentions comes first.
 *
 * @author Jomi and Rafael
 */
public class Intention implements Serializable, Comparable<Intention>, Iterable<IntendedMeans> {

    private static final long serialVersionUID = 1L;
    public  static final Intention EmptyInt = null;
    private static AtomicInteger idCount = new AtomicInteger(0);

    private int     id;
    private int     atomicCount    = 0; // number of atomic intended means in the intention
    private boolean isSuspended = false; // suspended by the internal action .suspend
    private String  suspendedReason = null;

    private Deque<IntendedMeans> intendedMeans = new ArrayDeque<>();

    //private Trigger initialTrigger = null; // just for additional information/debug (not really necessary)

    //static private Logger logger = Logger.getLogger(Intention.class.getName());

    public Intention() {
        id = idCount.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    public void push(IntendedMeans im) {
        intendedMeans.push(im);
        if (im.isAtomic())
            atomicCount++;
        //if (initialTrigger == null)
        //    initialTrigger = im.getTrigger();
    }

    public IntendedMeans peek() {
        return intendedMeans.peek();
    }

    public IntendedMeans pop() {
        IntendedMeans top = intendedMeans.pop();

        if (isAtomic() && top.isAtomic())
            atomicCount--;

        return top;
    }

    public boolean isAtomic() {
        return atomicCount > 0;
    }

    public void setAtomic(int a) { // used for testing
        atomicCount = a;
    }

    public Iterator<IntendedMeans> iterator() {
        return intendedMeans.iterator();
    }

    public boolean isFinished() {
        return intendedMeans.isEmpty();
    }

    public int size() {
        return intendedMeans.size();
    }

    public void clearIM() {
        intendedMeans.clear();
    }

    public void setSuspended(boolean b) {
        isSuspended = b;
        if (!b)
            suspendedReason = null;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspendedReason(String r) {
        suspendedReason = r;
    }
    public String getSuspendedReason() {
        if (suspendedReason == null)
            return "";
        else
            return suspendedReason;
    }

    /** returns the IntendedMeans that succeeds in test c, returns null if there isn't one */
    public IntendedMeans getIM(IMCondition c, Unifier u) { //Trigger g, Unifier u) {
        for (IntendedMeans im : intendedMeans)
            //System.out.println(g + " = "+ im.getTrigger()+" = "+u.unifies(g, im.getTrigger()));
            if (c.test(im,u)) //u.unifies(g, im.getTrigger()))
                return im;
        return null;
    }

    public IntendedMeans getBottom() {
        return intendedMeans.getLast();
    }

    /** returns true if the intention has an IM where TE = g, using u to verify equality */
    public boolean hasTrigger(Trigger g, Unifier u) {
        //return getIM(g,u) != null;
        for (IntendedMeans im : intendedMeans)
            if (u.unifies(g, im.getTrigger()))
                return true;
        return false;
    }

    /** remove all IMs until the lowest IM that succeeds in test c */
    public IntendedMeans dropGoal(IMCondition c, Unifier u) {
        IntendedMeans r = null;
        IntendedMeans im = getIM(c,u);
        while (im != null) {
            r = im;
            // remove the IMs until im-1
            while (peek() != im) {
                pop();
            }
            pop(); // remove im
            im = getIM(c,u); // keep removing other occurrences of te
        }
        return r;
    }

    public void fail(Circumstance c) {
    }

    public Pair<Event, Integer> findEventForFailure(Trigger tevent, PlanLibrary pl, Circumstance c) {
        Iterator<IntendedMeans> ii = iterator();
        IntendedMeans im;
        int posInStak = size();
        if (tevent.isGoal() && !tevent.isAddition()) { // case of failure in a fail plan
            im = ii.next();
            // find its goal in the stack
            boolean found = false;
            while (ii.hasNext() && !found) {
                im = ii.next();
                posInStak--;
                while (ii.hasNext() && im.unif.unifies(im.getTrigger().getLiteral(),tevent.getLiteral())) {
                    im = ii.next();
                    posInStak--;
                    found = true;
                }
            }
            tevent = im.getTrigger();
        }
        Trigger failTrigger = new Trigger(TEOperator.del, tevent.getType(), tevent.getLiteral());
        synchronized (pl.getLock()) {
            while (!pl.hasCandidatePlan(failTrigger) && ii.hasNext()) {
                // TODO: pop IM until +!g or *!g (this TODO is valid only if meta events are pushed on top of the intention)
                // If *!g is found first, no failure event
                // - while popping, if some meta event (* > !) is in the stack, stop and simple pop instead of producing an failure event
                im = ii.next();
                tevent = im.getTrigger();
                failTrigger = new Trigger(TEOperator.del, tevent.getType(), tevent.getLiteral());
                posInStak--;
            }
            if (tevent.isGoal() && //tevent.isAddition() &&
                    pl.hasCandidatePlan(failTrigger))
                return new Pair<>(new Event(failTrigger.clone(), this), posInStak);
            else
                return new Pair<>(null, 0);
        }
    }


    /** atomic intentions are grater than not atomic intentions */
    public int compareTo(Intention o) {
        if (o.atomicCount > this.atomicCount) return 1;
        if (this.atomicCount > o.atomicCount) return -1;

        if (o.id > this.id) return 1;
        if (this.id > o.id) return -1;
        return 0;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Intention) return ((Intention)o).id == this.id;
        return false;
    }

    public int hashCode() {
        return id;
    }

    public Intention clone() {
        Intention i = new Intention();
        i.id = id;
        i.atomicCount = atomicCount;
        i.intendedMeans = new ArrayDeque<>();
        for (IntendedMeans im: intendedMeans) {
            i.intendedMeans.add((IntendedMeans)im.clone());
        }
        return i;
    }

    // used by fork
    public void copyTo(Intention i) {
        i.atomicCount   = atomicCount;
        i.intendedMeans = new ArrayDeque<>(intendedMeans);
    }

    public String toString() {
        StringBuilder s = new StringBuilder("intention "+id+": \n");
        int i = 0;
        for (IntendedMeans im: intendedMeans) {
            s.append("    " + im + "\n");
            if (i++ > 40) {
                s.append("... more "+ (size()-40) + " intended means!\n");
                break;
            }
        }
        if (isFinished())
            s.append("<finished intention>");
        return s.toString();
    }

    public Term getAsTerm() {
        Structure intention = new Structure("intention");
        intention.addTerm(new NumberTermImpl(getId()));
        ListTerm lt = new ListTermImpl();
        for (IntendedMeans im: intendedMeans)
            lt.add(im.getAsTerm());
        intention.addTerm(lt);
        return intention;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element eint = (Element) document.createElement("intention");
        eint.setAttribute("id", id + "");
        for (IntendedMeans im: intendedMeans)
            eint.appendChild(im.getAsDOM(document));
        //if (intendedMeans.isEmpty())
        //    eint.appendChild( initialTrigger.getAsDOM(document));
        eint.setAttribute("finished", ""+isFinished());
        eint.setAttribute("suspended", ""+isSuspended());

        return eint;
    }

}
