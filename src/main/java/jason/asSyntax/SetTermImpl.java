package jason.asSyntax;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;


/**
 * Represents a Jason set as a TreeSet in java
 *
 *
 * @author Jomi
 */
public class SetTermImpl extends Structure implements SetTerm {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(SetTermImpl.class.getName());

    public static final String SET_FUNCTOR = "set";
    private Set<Term> set;

    public SetTermImpl() {
        super(SET_FUNCTOR, 0);
        set = new TreeSet<Term>();
    }


    /** make a hard copy of the terms */
    public SetTerm clone() {
        SetTermImpl t = new SetTermImpl();
        for (Term a: this.set) 
        	t.set.add(a.clone());
        t.hashCodeCache = this.hashCodeCache;
        return t;
    }

    /** make a hard copy of the terms */
    @Override
    public SetTerm capply(Unifier u) {
        /*SetTermImpl t = new SetTermImpl();
        for (Term a: this.set) 
        	t.set.add(a.capply(u));
        return t;*/
    	return this; // TODO: think about this!
    }

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;

        if (t instanceof Term &&  ((Term)t).isVar() ) return false; // unground var is not equals a list
        if (t instanceof SetTerm) {
        	// TODO: implement
            /*ListTerm tAsList = (ListTerm)t;
            if (term == null && tAsList.getTerm() != null) return false;
            if (term != null && !term.equals(tAsList.getTerm())) return false;
            if (next == null && tAsList.getNext() != null) return false;
            if (next != null) return next.equals(tAsList.getNext());
            return true;*/
        }
        return false;
    }

    @Override
    public int calcHashCode() {
    	return set.hashCode();
    }

    @Override
    public int compareTo(Term o) {
        if (o instanceof VarTerm)
            return o.compareTo(this) * -1;
        if ((o instanceof NumberTerm))
            return 1;
        if (o instanceof StringTerm)
            return 1;
        return super.compareTo(o);
    }

    // for unifier compatibility
    @Override
    public int getArity() {
    	return 1;
    }

    // for unifier compatibility
    @Override
    public Term getTerm(int i) {
        return null; // TODO: implement
    }

    public int size() {
    	return set.size();
    }

    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public boolean isSet() {
        return true;
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    public boolean isGround() {
        // TODO:
        return false;
    }

    @Override
    public boolean hasVar(VarTerm t, Unifier u) {
    	// TODO 
    	return false;
    }
    
    @Override
    public Literal makeVarsAnnon() {
    	return this;
    }
    @Override
    public Literal makeVarsAnnon(Unifier un) {
    	return this;
    }
    
    @Override
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un) {
        logger.log(Level.WARNING, "ListTermImpl cannot be used for logical consequence!", new Exception());
        return LogExpr.EMPTY_UNIF_LIST.iterator();
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
            s.append(v+t);
            v = ",";
        }
        s.append('}');
        return s.toString();
    }

    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("set-term");
        String c = "";
        for (Term t: this) {
            Element et = t.getAsDOM(document);
            et.setAttribute("sep", c);
            c = ",";
            u.appendChild(et);
        }
        return u;
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
