package jason.asSyntax;

import jason.asSemantics.Unifier;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents an atom (a positive literal with no argument and no annotation, e.g. "tell", "a").
 */
public class Atom extends Literal {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Atom.class.getName());

    private final String functor; // immutable field
    private final Atom   ns; // name space

    public Atom(String functor) {
        this(DefaultNS, functor);
    }

    protected Atom(Atom namespace, String functor) {
        if (functor == null)
            logger.log(Level.WARNING, "The functor of an atom functor should not be null!", new Exception());
        this.functor = functor;
        this.ns      = namespace;
    }

    public Atom(Literal l) {
        this(l.getNS(), l);
    }

    public Atom(Literal l, Unifier u) {
        this((Atom)l.getNS().capply(u), l);
    }

    public Atom(Atom namespace, Literal l) {
        this.functor            = l.getFunctor();
        this.ns                 = namespace;
        //predicateIndicatorCache = l.predicateIndicatorCache;
        //hashCodeCache           = l.hashCodeCache;
        srcInfo                 = l.srcInfo;
    }

    public String getFunctor() {
        return functor;
    }

    public Atom getNS() {
        return ns;
    }

    public Term clone() {
        return this; // since this object is immutable
    }

    @Override
    public Term capply(Unifier u) {
        if (ns.isVar())
            return new Atom(this, u);
        else
            return this;
    }

    @Override
    public Literal cloneNS(Atom newnamespace) {
        return new Atom(newnamespace, this);
    }

    @Override
    public boolean isAtom() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Atom) {
            Atom a = (Atom)o;
            //System.out.println(getFunctor() +" ==== " + a.getFunctor() + " atom "+ a.isAtom() + " ns " + getNS() + "/" + a.getNS()); // && getFunctor().equals(a.getFunctor())));
            return a.isAtom() && getFunctor().equals(a.getFunctor()) && getNS().equals(a.getNS());
        }
        return false;
    }

    public int compareTo(Term t) {
        if (t == null) return -1; // null should be first (required for addAnnot)
        if (t.isNumeric()) return 1;

        // this is a list and the other not
        if (isList() && !t.isList()) return -1;

        // this is not a list and the other is
        if (!isList() && t.isList()) return 1;

        // both are lists, check the size
        if (isList() && t.isList()) {
            ListTerm l1 = (ListTerm)this;
            ListTerm l2 = (ListTerm)t;
            final int l1s = l1.size();
            final int l2s = l2.size();
            if (l1s > l2s) return 1;
            if (l2s > l1s) return -1;
            return 0; // need to check elements (in Structure class)
        }
        if (t.isVar())
            return -1;
        if (t instanceof Literal) {
            Literal tAsLit = (Literal)t;
            if (getNS().equals(tAsLit.getNS())) { // same ns
                final int ma = getArity();
                final int oa = tAsLit.getArity();
                if (ma < oa)
                    return -1;
                else if (ma > oa)
                    return 1;
                else
                    return getFunctor().compareTo(tAsLit.getFunctor());
            } else {
                return getNS().compareTo(tAsLit.getNS());
            }
        }

        return super.compareTo(t);
    }

    @Override
    protected int calcHashCode() {
        return getFunctor().hashCode() + getNS().hashCode();
    }

    @Override
    public String toString() {
        if (ns == DefaultNS)
            return functor;
        else
            return getNS() + "::" + functor;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("structure");
        u.setAttribute("functor",getFunctor());
        u.setAttribute("name-space", getNS().getFunctor());
        return u;
    }
}
