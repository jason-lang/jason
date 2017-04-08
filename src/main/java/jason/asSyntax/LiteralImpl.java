package jason.asSyntax;

import jason.asSemantics.Unifier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A Literal extends a Pred with strong negation (~).
 */
public class LiteralImpl extends Pred {

    private static final long serialVersionUID = 1L;
    //private static Logger logger = Logger.getLogger(LiteralImpl.class.getName());

    private boolean type = LPos;

    /** creates a positive literal */
    public LiteralImpl(String functor) {
        super(functor);
    }

    /** if pos == true, the literal is positive, otherwise it is negative */
    public LiteralImpl(boolean pos, String functor) {
        super(functor);
        type = pos;
    }

    public LiteralImpl(Literal l) {
        super(l);
        type = !l.negated();
    }

    // used by capply
    protected LiteralImpl(Literal l, Unifier u) {
        super(l, u);
        type = !l.negated();
    }


    /** if pos == true, the literal is positive, otherwise it is negative */
    public LiteralImpl(boolean pos, Literal l) {
        super(l);
        type = pos;
    }

    /** if pos == true, the literal is positive, otherwise it is negative */
    public LiteralImpl(Atom namespace, boolean pos, String functor) {
        super(namespace, functor);
        type = pos;
    }

    /** creates a literal based on another but in another name space and signal */
    public LiteralImpl(Atom namespace, boolean pos, Literal l) {
        super(namespace, l);
        type = pos;
    }

    @Override
    public boolean isAtom() {
        return super.isAtom() && !negated();
    }

    /** to be overridden by subclasses (as internal action) */
    @Override
    public boolean canBeAddedInBB() {
        return true;
    }

    @Override
    public boolean negated() {
        return type == LNeg;
    }

    public Literal setNegated(boolean b) {
        type = b;
        resetHashCodeCache();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof LiteralImpl) {
            final LiteralImpl l = (LiteralImpl) o;
            return type == l.type && hashCode() == l.hashCode() && super.equals(l);
        } else if (o instanceof Atom && !negated()) {
            return super.equals(o);
        }
        return false;
    }

    @Override
    public String getErrorMsg() {
        String src = getSrcInfo() == null ? "" : " ("+ getSrcInfo() + ")";
        return "Error in '"+this+"'"+src;
    }

    @Override
    public int compareTo(Term t) {
        if (t == null)
            return -1;
        if (t.isLiteral()) {
            Literal tl = (Literal)t;
            if (!negated() && tl.negated())
                return -1;
            else if (negated() && !tl.negated())
                return 1;
        }
        return super.compareTo(t);
    }

    @Override
    public Term clone() {
        Literal l = new LiteralImpl(this);
        l.hashCodeCache = this.hashCodeCache;
        return l;
    }

    @Override
    public Term capply(Unifier u) {
        return new LiteralImpl(this, u);
    }

    public Literal cloneNS(Atom newNameSpace) {
        return new LiteralImpl(newNameSpace, !negated(), this);
    }

    @Override
    protected int calcHashCode() {
        int result = super.calcHashCode();
        if (negated()) result += 3271;
        return result;
    }

    /** returns [~] super.getPredicateIndicator */
    @Override
    public PredicateIndicator getPredicateIndicator() {
        if (predicateIndicatorCache == null)
            predicateIndicatorCache = new PredicateIndicator(getNS(), ((type == LPos) ? getFunctor() : "~"+getFunctor()),getArity());
        return predicateIndicatorCache;
    }

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("literal");
        u.setAttribute("namespace", getNS().getFunctor());
        if (negated()) {
            u.setAttribute("negated", negated()+"");
        }
        u.appendChild(super.getAsDOM(document));
        return u;
    }
}
