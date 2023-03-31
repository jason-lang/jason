package jason.asSyntax;

import java.io.Serial;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSemantics.Unifier;


/**
 * Represents a Jason set as a TreeSet in java
 *
 *
 * @author Jomi
 */
public class SetTermImpl extends DefaultTerm implements SetTerm {

    @Serial
    private static final long serialVersionUID = 1L;
    //private static Logger logger = Logger.getLogger(SetTermImpl.class.getName());

    private Set<Term> set;

    public SetTermImpl() {
        set = new TreeSet<>();
    }


    @Override
    public SetTerm clone() {
        return this;
    }

    @Override
    public SetTerm deepClone() {
        SetTermImpl t = new SetTermImpl();
        for (Term a: this.set)
            t.set.add(a.clone());
        t.hashCodeCache = this.hashCodeCache;
        return t;
    }

    @Override
    public SetTerm capply(Unifier u) {
        return this; // TODO: think about this!
    }

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;
        if (t instanceof SetTerm) return this.set.equals( ((SetTermImpl)t).set );
        return false;
    }

    @Override
    public int calcHashCode() {
        return set.hashCode();
    }

    @Override
    public int compareTo(Term o) {
        if (o instanceof NumberTerm || o instanceof StringTerm || o instanceof Literal)
            return 1;
        return super.compareTo(o);
    }


    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isSet() {
        return true;
    }

    // copy the set to a new list
    public ListTerm getAsListTerm() {
        ListTerm result = new ListTermImpl();
        ListTerm tail = result;
        for (Term t: set)
            tail = tail.append(t.clone());
        return result;
    }

    public Iterator<Term> iterator() {
        return set.iterator();
    }


    @Override
    public boolean add(Term t) {
        return set.add(t);
    }

    @Override
    public void union(Iterable<Term> lt) {
        for (Term t: lt) {
            add(t);
        }
    }

    @Override
    public void intersection(Collection<Term> lt) {
        set.retainAll(lt);
    }

    @Override
    public void difference(Collection<Term> lt) {
        set.removeAll(lt);
    }


    public String toString() {
        StringBuilder s = new StringBuilder("{");
        String v = "";
        for (Term t: this) {
            s.append(v).append(t);
            v = ",";
        }
        s.append('}');
        return s.toString();
    }

    public Element getAsDOM(Document document) {
        Element u = document.createElement("set-term");
        String c = "";
        for (Term t: this) {
            Element et = t.getAsDOM(document);
            et.setAttribute("sep", c);
            c = ",";
            u.appendChild(et);
        }
        return u;
    }

    /*@Override
    public String getAsJSON(String identation) {
        StringBuilder json = new StringBuilder("[\n");
        String c = "";
        for (Term t: this) {
            json.append(c+"  "+ t.getAsJSON(identation+"   ") );
            c = ",\n";
        }
        json.append("\n]");
        return json.toString();
    }*/

    @Override
    public JsonValue getAsJson() {
        JsonArrayBuilder bterms = Json.createArrayBuilder();
        for (Term t: this)
            bterms.add( t.getAsJson());
        return bterms.build();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }


    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }


    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }


    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }


    @Override
    public boolean addAll(Collection<? extends Term> c) {
        return set.addAll(c);
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        return set.removeAll(c);
    }


    @Override
    public void clear() {
        set.clear();
    }
}
