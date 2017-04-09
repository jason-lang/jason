package jason.asSyntax;

import java.io.Serializable;

/**
 * Represents the "type" of a predicate based on the functor and the arity, e.g.: ask/4
 *
 * @author jomi
 */
public final class PredicateIndicator implements Comparable<PredicateIndicator>, Serializable {

    private final String functor;
    private final int    arity;
    private final int    hash;
    private final Atom   ns;

    public PredicateIndicator(String functor, int arity) {
        this(Literal.DefaultNS, functor, arity);
    }

    public PredicateIndicator(Atom ns, String functor, int arity) {
        this.functor = functor;
        this.arity   = arity;
        this.ns      = ns;
        hash         = calcHash();
    }

    public String getFunctor() {
        return functor;
    }

    public int getArity() {
        return arity;
    }

    public Atom getNS() {
        return ns;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o != null && o instanceof PredicateIndicator && o.hashCode() == this.hashCode()) {
            final PredicateIndicator pi = (PredicateIndicator)o;
            return arity == pi.arity && functor.equals(pi.functor) && ns.equals(pi.ns);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(PredicateIndicator pi) {
        int c = this.ns.compareTo(pi.ns);
        if (c != 0) return c;

        c = this.functor.compareTo(pi.functor);
        if (c != 0) return c;

        if (pi.arity > this.arity) return -1;
        if (this.arity > pi.arity) return 1;
        return 0;
    }

    private int calcHash() {
        int t  = 31 * arity * ns.hashCode();
        if (functor != null) t = 31 * t + functor.hashCode();
        return t;
    }

    public String toString() {
        if (ns == Literal.DefaultNS)
            return functor + "/" + arity;
        else
            return ns + "::" + functor + "/" + arity;
    }
}
