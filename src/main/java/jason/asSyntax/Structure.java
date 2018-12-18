package jason.asSyntax;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;
import jason.util.Config;

/**
 * Represents a structure: a functor with <i>n</i> arguments,
 * e.g.: val(10,x(3)).
 *
 * @composed - terms 0..* Term
 */
public class Structure extends Atom {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Structure.class.getName());

    protected static final List<Term> emptyTermList  = new ArrayList<Term>(0);
    protected static final Term[]     emptyTermArray = new Term[0]; // just to have a type for toArray in the getTermsArray method

    private List<Term> terms;
    //protected Boolean isGround = true; // it seems to not improve the performance


    public Structure(String functor) {
        //this.functor = (functor == null ? null : functor.intern()); // it does not improve performance in test i did!
        this(DefaultNS, functor);
    }

    public Structure(Literal l) {
        this(l.getNS(), l);
    }


    public Structure(Atom namespace, String functor) {
        //this.functor = (functor == null ? null : functor.intern()); // it does not improve performance in test i did!
        super(namespace, functor);
    }

    public Structure(Atom namespace, Literal l) {
        super(namespace, l);
        final int tss = l.getArity();
        if (tss > 0) {
            terms = new ArrayList<Term>(tss);
            for (int i = 0; i < tss; i++)
                terms.add(l.getTerm(i).clone());
        }
        //isGround = null;
    }

    // used by capply
    protected Structure(Literal l, Unifier u) {
        super(l, u);
        final int tss = l.getArity();
        if (tss > 0) {
            terms = new ArrayList<Term>(tss);
            for (int i = 0; i < tss; i++)
                terms.add(l.getTerm(i).capply(u));
        }
        resetHashCodeCache();
        //isGround = null;
    }

    /**
     * Create a structure with a defined number of terms.
     *
     * It is used by list term, plan body, ... to not create the array list for terms.
     */
    public Structure(String functor, int termsSize) {
        super(functor);
        if (termsSize > 0)
            terms = new ArrayList<Term>(termsSize);
    }

    public static Structure parse(String sTerm) {
        as2j parser = new as2j(new StringReader(sTerm));
        try {
            Term t = parser.term();
            if (t instanceof Structure)
                return (Structure)t;
            else
                return new Structure((Atom)t);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing structure " + sTerm,e);
            return null;
        }
    }

    @Override
    protected int calcHashCode() {
        int result = super.calcHashCode();
        final int ts = getArity();
        for (int i=0; i<ts; i++)
            result = 7 * result + getTerm(i).hashCode();
        return result;
    }

    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;

        if (t instanceof Structure) {
            Structure tAsStruct = (Structure)t;

            // if t is a VarTerm, uses var's equals
            if (tAsStruct.isVar())
                return ((VarTerm)t).equals(this);

            final int ts = getArity();
            if (ts != tAsStruct.getArity())
                return false;

            if (!getFunctor().equals(tAsStruct.getFunctor()))
                return false;

            if (!getNS().equals(tAsStruct.getNS()))
                return false;

            for (int i=0; i<ts; i++)
                if (!getTerm(i).equals(tAsStruct.getTerm(i)))
                    return false;

            return true;
        }
        if (t instanceof Atom && this.isAtom()) {
            // consider atom equals only when this is an atom
            return super.equals(t);
        }
        return false;
    }

    public int compareTo(Term t) {
        int c = super.compareTo(t);
        if (c != 0)
            return c;

        if (t.isStructure()) {
            Structure tAsStruct = (Structure)t;

            final int ma = getArity();
            final int oa = tAsStruct.getArity();
            for (int i=0; i<ma && i<oa; i++) {
                c = getTerm(i).compareTo(tAsStruct.getTerm(i));
                if (c != 0)
                    return c;
            }
        }
        return 0;
    }

    @Override
    public boolean subsumes(Term t) {
        if (t.isStructure()) {
            Structure tAsStruct = (Structure)t;

            final int ma = getArity();
            final int oa = tAsStruct.getArity();
            for (int i=0; i<ma && i<oa; i++) {
                //System.out.println(getTerm(i)+" comp "+tAsStruct.getTerm(i)+"="+getTerm(i).isMoreGeneral(tAsStruct.getTerm(i)));
                if (! getTerm(i).subsumes(tAsStruct.getTerm(i)))
                    return false;
            }
            return true;
        } else {
            return super.subsumes(t);
        }
    }

    @Override
    public Term capply(Unifier u) {
        return new Structure(this, u);
    }

    /** make a deep copy of the terms */
    public Term clone() {
        Structure s = new Structure(this);
        s.hashCodeCache = this.hashCodeCache;
        return s;
    }

    @Override
    public Literal cloneNS(Atom newnamespace) {
        return new Structure(newnamespace, this);
    }

    @Override
    public void addTerm(Term t) {
        if (t == null) return;
        if (terms == null) terms = new ArrayList<Term>(5);
        terms.add(t);
        //if (!t.isGround())
        //    isGround = false;
        predicateIndicatorCache = null;
        resetHashCodeCache();
    }

    @Override
    public void delTerm(int index) {
        if (terms == null) return;
        terms.remove(index);
        predicateIndicatorCache = null;
        resetHashCodeCache();
        //isGround = null;
    }

    @Override
    public Literal addTerms(Term ... ts ) {
        if (terms == null) terms = new ArrayList<Term>(5);
        for (Term t: ts)
            terms.add(t);
        predicateIndicatorCache = null;
        resetHashCodeCache();
        return this;
    }

    @Override
    public Literal addTerms(List<Term> l) {
        if (terms == null) terms = new ArrayList<Term>(5);
        for (Term t: l)
            terms.add(t);
        predicateIndicatorCache = null;
        resetHashCodeCache();
        return this;
    }

    @Override
    public Literal setTerms(List<Term> l) {
        terms = l;
        predicateIndicatorCache = null;
        resetHashCodeCache();
        //isGround = null;
        return this;
    }

    @Override
    public void setTerm(int i, Term t) {
        if (terms == null) terms = new ArrayList<Term>(5);
        terms.set(i,t);
        resetHashCodeCache();
        //if (!t.isGround() && isGround())
        //    isGround = false;
    }

    public Term getTerm(int i) {
        if (terms == null)
            return null;
        else
            return terms.get(i);
    }

    @Override
    public int getArity() {
        if (terms == null)
            return 0;
        else
            return terms.size();
    }

    /** @deprecated use getArity */
    public int getTermsSize() {
        return getArity();
    }

    @Override
    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public boolean hasTerm() {
        return getArity() > 0; // should use getArity to work for list
    }

    @Override
    public boolean isStructure() {
        return true;
    }

    @Override
    public boolean isAtom() {
        return !hasTerm();
    }

    @Override
    public boolean isGround() {
        //if (isGround == null) {
        //    isGround = true;
        final int size = getArity();
        for (int i=0; i<size; i++) {
            if (!getTerm(i).isGround()) {
                //isGround = false;
                return false;
                //break;
            }
        }
        //}
        //return isGround;
        return true;
    }

    public boolean isUnary() {
        return getArity() == 1;
    }

    @Override
    public Literal makeVarsAnnon() {
        return makeVarsAnnon(new Unifier());
    }

    @Override
    public Literal makeVarsAnnon(Unifier un) {
        final int size = getArity();
        for (int i=0; i<size; i++) {
            Term ti = getTerm(i);
            if (ti.isVar())
                setTerm(i,varToReplace(ti, un));
            else if (ti instanceof Structure)
                ((Structure)ti).makeVarsAnnon(un);
        }
        resetHashCodeCache();
        return this;
    }

    private final static boolean useShortUnnamedVars = Config.get().getBoolean(Config.SHORT_UNNAMED_VARS);

    public VarTerm varToReplace(Term t, Unifier un) {
        VarTerm vt    = (VarTerm)t;
        VarTerm deref = un.deref(vt);
        //if (deref.isUnnamedVar())
        //return new UnnamedVar();

        // if the variable hasn't been renamed given the input unifier, then rename it.
        if (deref.equals(vt)) {
            // forget the name
            Atom ns = vt.getNS();
            if (ns.isVar())
                ns = varToReplace(ns, un);
            UnnamedVar var = useShortUnnamedVars ? new UnnamedVar(ns) : UnnamedVar.create(ns, t.toString());
            //var.setFromMakeVarAnnon();

            // if deref has annotations then we need to replicate these in the new variable
            if (deref.hasAnnot()) {
                var.setAnnots(deref.getAnnots().cloneLT());
                var.makeVarsAnnon(un);
            }
            un.bind(deref, var);
            return var;
        } else {
            // otherwise it has already been renamed in this scope so return
            // the existing renaming
            Atom ns = vt.getNS();
            if (ns.isVar())
                ns = varToReplace(ns, un);
            deref = (VarTerm)deref.cloneNS(ns);
            // ensure that if the input term has an annotation and the existing
            // renaming doesn't then we add the anonymized annotations
            if (vt.hasAnnot() && !deref.hasAnnot()) {
                deref.setAnnots(vt.getAnnots().cloneLT());
                deref.makeVarsAnnon(un);
            }
            return deref;
        }
    }

    /*
    protected VarTerm varToReplace(Term t, Unifier un) {
        if (t.isVar() && !t.isUnnamedVar()) {
            // replace t to an unnamed var
            VarTerm vt = un.deref((VarTerm)t);
            if (vt.isUnnamedVar()) {
                return (UnnamedVar)vt.clone();
            } else {
                UnnamedVar uv = new UnnamedVar( "_"+UnnamedVar.getUniqueId()+t);
                un.bind(vt, uv);
                return uv;
            }
        } else {
            return null;
        }
    }
    */

    @Override
    public void makeTermsAnnon() {
        final int size = getArity();
        for (int i=0; i<size; i++)
            setTerm(i,new UnnamedVar());
        resetHashCodeCache();
    }

    @Override
    public boolean hasVar(VarTerm t, Unifier u) {
        final int size = getArity();
        for (int i=0; i<size; i++)
            if (getTerm(i).hasVar(t, u))
                return true;
        return false;
    }

    public List<VarTerm> getSingletonVars() {
        Map<VarTerm, Integer> all  = new HashMap<VarTerm, Integer>();
        countVars(all);
        List<VarTerm> r = new ArrayList<VarTerm>();
        for (VarTerm k: all.keySet()) {
            if (all.get(k) == 1 && !k.isUnnamedVar())
                r.add(k);
        }
        return r;
    }

    public void countVars(Map<VarTerm, Integer> c) {
        final int tss = getArity();
        for (int i = 0; i < tss; i++)
            getTerm(i).countVars(c);
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        if (getNS() != DefaultNS) {
            s.append(getNS());
            s.append("::");
        }
        if (negated())
            s.append("~");
        if (getFunctor() != null)
            s.append(getFunctor());
        if (getArity() > 0) {
            s.append('(');
            Iterator<Term> i = terms.iterator();
            while (i.hasNext()) {
                s.append(i.next());
                if (i.hasNext()) s.append(',');
            }
            s.append(')');
        }
        if (hasAnnot())
            s.append(getAnnots().toString());
        return s.toString();
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("structure");
        u.setAttribute("functor",getFunctor());
        if (hasTerm()) {
            Element ea = document.createElement("arguments");
            for (Term t: getTerms()) {
                ea.appendChild(t.getAsDOM(document));
            }
            u.appendChild(ea);
        }
        return u;
    }
}
