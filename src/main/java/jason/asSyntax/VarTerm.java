package jason.asSyntax;

import jason.NoValueException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a variable Term: like X (starts with upper case). It may have a
 * value, after {@link VarTerm}.apply.
 *
 * An object of this class can be used in place of a
 * Literal, Number, List, String, .... It behaves like a
 * Literal, Number, .... just in case its value is a Literal,
 * Number, ...
 *
 * @author jomi
 */
public class VarTerm extends LiteralImpl implements NumberTerm, ListTerm { //, StringTerm, ObjectTerm, PlanBody {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(VarTerm.class.getName());

    //private Term value  = null;

    public VarTerm(String s) {
        super(s);
        if (s != null && Character.isLowerCase(s.charAt(0))) {
            logger.warning("Are you sure you want to create a VarTerm that begins with lowercase (" + s + ")? Should it be a Term instead?");
            Exception e = new Exception("stack");
            e.printStackTrace();
        }
    }

    public VarTerm(Atom namespace, String functor) {
        super(namespace, LPos, functor);
    }

    public VarTerm(Atom namespace, Literal v) {
        super(namespace, !v.negated(), v);
    }

    /** @deprecated prefer ASSyntax.parseVar(...) */
    public static VarTerm parseVar(String sVar) {
        as2j parser = new as2j(new StringReader(sVar));
        try {
            return parser.var(Literal.DefaultNS);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing var " + sVar, e);
            return null;
        }
    }

    @Override
    public Term capply(Unifier u) {
        if (u != null) {
            Term vl = u.get(this);
            if (vl != null) {
                if (!vl.isCyclicTerm() && vl.hasVar(this, u)) {
                    //logger.warning("The value of a variable contains itself, variable "+super.getFunctor()+" "+super.getSrcInfo()+", value="+vl+", unifier="+u);

                    u.remove(this); // remove this var to avoid loops in the apply below
                    Term tempVl = vl.capply(u);
                    u.bind(this, vl);

                    CyclicTerm ct = new CyclicTerm((Literal)tempVl, this);
                    Unifier renamedVars = new Unifier(); // remove "this" from the value to avoid loops in apply
                    ct.makeVarsAnnon(renamedVars);
                    renamedVars.remove(this);
                    u.compose(renamedVars);
                    vl = ct;
                }

                vl = vl.capply(u); // should clone here, since there is no cloning in unify

                if (vl.isLiteral()) {
                    if (getNS() != Literal.DefaultNS) {
                        // use var ns for the value ns
                        vl = ((Literal)vl).cloneNS(  (Atom)getNS().capply(u) ); // this var ns could be a var, so capply
                    }
                    if (negated()) {
                        ((Literal)vl).setNegated(Literal.LNeg);
                    }
                }


                // decide whether to use var annots in apply
                //   X = p[a]
                //   !X[b]
                // what's the event:
                //   +!p[a]
                // or
                //   +!p[a,b]
                // Answer: use annots of var, useful for meta-programming like
                //         P[step(N)]
                if (vl.isLiteral() && this.hasAnnot()) { // if this var has annots, add them in the value's annots (Experimental)
                    vl = ((Literal)vl).forceFullLiteralImpl().addAnnots((ListTerm)this.getAnnots().capply(u));
                }
                return vl;
            }
        }
        return clone();
    }

    public Term clone() {
        return new VarTerm(this.getNS(), this);
    }

    @Override
    public Literal cloneNS(Atom newNameSpace) {
        return new VarTerm(newNameSpace, this);
    }

    public ListTerm cloneLT() {
        return (ListTerm)clone();
    }

    @Override
    public boolean isVar() {
        return true;
    }

    public boolean isUnnamedVar() {
        return false;
    }

    @Override
    public boolean isGround() {
        return false;
    }

    // DO NOT consider ns in equals and hashcode!
    // idem for negated
    // in the unifier, the vars have no ns neither negation

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;
        // is t also a var? (its value must also be null)
        if (t instanceof VarTerm) {
            final VarTerm tAsVT = (VarTerm) t;
            return //negated() == tAsVT.negated() &&
                getFunctor().equals(tAsVT.getFunctor()); // && getNS().equals(tAsVT.getNS());
        }
        return false;
    }

    @Override
    protected int calcHashCode() {
        int result = getFunctor().hashCode();
        //if (negated()) result += 3271; // TODO: review
        // Do not consider NS and negated! (in unifier, A = ~A)
        return result;
    }


    public int compareTo(Term t) {
        if (t == null || t.isUnnamedVar())
            return -1;
        else if (t.isVar()) {
            //if (!negated() && ((VarTerm)t).negated())
            //    return -1;
            //else
            return getFunctor().compareTo(((VarTerm)t).getFunctor());
        } else {
            return 1;
        }
    }

    @Override
    public boolean subsumes(Term t) {
        return true;
    }

    // ----------
    // Term methods overridden
    //
    // in case this VarTerm has a value, use value's methods
    // ----------

    @Override
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un) {
        // try to apply
        Term t = this.capply(un);
        if ( t.equals(this) ) {
            // the variable is still a Var, find all bels that unify.
            return super.logicalConsequence(ag, un);
        } else {
            // the clone is still a var
            return ((LogicalFormula)t).logicalConsequence(ag, un);
        }
    }

    @Override
    public Term getTerm(int i) {
        return null;
    }

    @Override
    public void addTerm(Term t) {
        logger.log(Level.WARNING, "The addTerm '"+t+"' in "+this+" was lost, since I am a var.", new Exception());
    }


    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public List<Term> getTerms() {
        return null;
    }


    @Override
    public Literal setTerms(List<Term> l) {
        return this;
    }

    @Override
    public void setTerm(int i, Term t) {
    }

    @Override
    public Literal addTerms(List<Term> l) {
        return this;
    }

    @Override
    public boolean isInternalAction() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isPlanBody() {
        return false;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isPred() {
        return false;
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    @Override
    public boolean isStructure() {
        return false;
    }

    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public boolean isRule() {
        return false;
    }

    @Override
    public boolean isArithExpr() {
        return false;
    }

    @Override
    public boolean isCyclicTerm() {
        return false;
    }

    @Override
    public boolean hasVar(VarTerm t, Unifier u) {
        if (equals(t))
            return true;

        if (u != null) { // if the var has a value in the unifier, search in that value
            Term vl = u.get(this);
            if (vl != null) {
                try {
                    u.remove(this); // remove this var from the unifier to avoid going to search inside it again
                    return vl.hasVar(t, u);
                } finally {
                    u.bind(this, vl);
                }
            }

        }

        return false;
    }

    @Override
    public void countVars(Map<VarTerm, Integer> c) {
        int n = c.containsKey(this) ? c.get(this) : 0;
        c.put(this, n+1);
        super.countVars(c);
    }

    @Override
    public boolean canBeAddedInBB() {
        return false;
    }

    // ----------
    // ArithmeticExpression methods overridden
    // Interface NumberTerm
    // ----------

    public double solve() throws NoValueException {
        throw new NoValueException("Error evaluating "+this+". It is not ground!");
    }

    // ----------
    //
    // ListTerm methods overridden
    //
    // ----------

    public void add(int index, Term o) {
    }

    public boolean add(Term o) {
        return false;
    }

    public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
        return false;
    }

    public boolean addAll(int index, @SuppressWarnings("rawtypes") Collection c) {
        return false;
    }

    public void clear() {
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean containsAll(@SuppressWarnings("rawtypes") Collection c) {
        return false;
    }

    public Term get(int index) {
        return null;
    }

    public int indexOf(Object o) {
        return -1;
    }

    public int lastIndexOf(Object o) {
        return -1;
    }

    public Iterator<Term> iterator() {
        return null;
    }

    public ListIterator<Term> listIterator() {
        return null;
    }

    public ListIterator<Term> listIterator(int index) {
        return null;
    }

    public Term remove(int index) {
        return null;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean removeAll(@SuppressWarnings("rawtypes") Collection c) {
        return false;
    }

    public boolean retainAll(@SuppressWarnings("rawtypes") Collection c) {
        return false;
    }

    public Term set(int index, Term o) {
        return null;
    }

    public List<Term> subList(int arg0, int arg1) {
        return null;
    }

    public Iterator<List<Term>> subSets(int k) {
        return null;
    }

    public Object[] toArray() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] arg0) {
        return null;
    }

    // from ListTerm

    public void setTerm(Term t) {
    }

    public void setNext(Term t) {
    }

    public ListTerm append(Term t) {
        return null;
    }
    public ListTerm insert(Term t) {
        return null;
    }
    public ListTerm concat(ListTerm lt) {
        return null;
    }

    public ListTerm reverse() {
        return null;
    }

    public ListTerm union(ListTerm lt) {
        return null;
    }

    public ListTerm intersection(ListTerm lt) {
        return null;
    }

    public ListTerm difference(ListTerm lt) {
        return null;
    }

    public List<Term> getAsList() {
        return null;
    }

    public ListTerm getLast() {
        return null;
    }

    public ListTerm getPenultimate() {
        return null;
    }

    public Term removeLast() {
        return null;
    }

    public ListTerm getNext() {
        return null;
    }

    public Term getTerm() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isEnd() {
        return false;
    }

    public boolean isTail() {
        return false;
    }

    public void setTail(VarTerm v) {
    }

    public VarTerm getTail() {
        return null;
    }

    public Iterator<ListTerm> listTermIterator() {
        return null;
    }

    public int size() {
        return -1;
    }

    public ListTerm cloneLTShallow() {
        return null;
    }


    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("var-term");
        u.setAttribute("functor", getFunctor());
        if (hasAnnot()) {
            Element ea = document.createElement("annotations");
            ea.appendChild(getAnnots().getAsDOM(document));
            u.appendChild(ea);
        }
        return u;
    }
}
