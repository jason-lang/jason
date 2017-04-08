package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Pred extends a Structure with annotations, e.g.: a(1)[an1,an2].
 */
public class Pred extends Structure {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Pred.class.getName());

    private ListTerm      annots;

    public Pred(String functor) {
        super(functor);
    }

    public Pred(Literal l) {
        this(l.getNS(), l);
    }

    public Pred(Atom namespace, String functor) {
        super(namespace, functor);
    }

    public Pred(Atom namespace, Literal l) {
        super(namespace, l);

        if (l.hasAnnot()) {
            annots = l.getAnnots().cloneLT();
        } else {
            annots = null;
        }
    }

    // used by capply
    protected Pred(Literal l, Unifier u) {
        super(l, u);
        if (l.hasAnnot()) {
            setAnnots( (ListTerm)l.getAnnots().capply(u) );
        } else {
            annots = null;
        }
    }

    public Pred(String functor, int termsSize) {
        super(functor, termsSize);
    }

    public static Pred parsePred(String spread) {
        as2j parser = new as2j(new StringReader(spread));
        try {
            return parser.pred();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing predicate " + spread, e);
            return null;
        }
    }

    @Override
    public boolean isPred() {
        return true;
    }

    @Override
    public boolean isAtom() {
        return super.isAtom() && !hasAnnot();
    }

    @Override
    public boolean isGround() {
        if (annots == null) {
            return super.isGround();
        } else {
            return super.isGround() && annots.isGround();
        }
    }

    /*
    @Override
    public boolean apply(Unifier u) {
        boolean r1 = super.apply(u);
        boolean r2 = applyAnnots(u);
        return r1 || r2;
    }
    */
    /*
    private final boolean applyAnnots(Unifier u) {
        boolean r  = false;
        if (annots != null) {
            // if some annotation has variables that become ground, they need to be replaced in the list to maintain the order
            List<Term> toAdd = null;
            Iterator<ListTerm> i = annots.listTermIterator();
            while (i.hasNext()) {
                ListTerm lt = i.next();
                if (lt.isTail() && lt.getTail().apply(u)) { // have to test tail before term, since term test may lead to i.remove that remove also the tail
                    r = true;
                    lt.getTerm().apply(u); // apply for the term
                    setAnnots(annots); // sort all annots given from tail ground
                    break; // the iterator is inconsistent
                } else if (lt.getTerm() != null && lt.getTerm().apply(u)) {
                    r = true;
                    if (toAdd == null)
                        toAdd = new ArrayList<Term>();
                    toAdd.add( lt.getTerm() );
                    i.remove();
                }
            }
            if (toAdd != null)
                for (Term t: toAdd)
                    addAnnot(t);
        }
        return r;
    }
    */

    @Override
    public Literal setAnnots(ListTerm l) {
        annots = null;
        if (l == null)
            return this;
        Iterator<ListTerm> i = l.listTermIterator();
        while (i.hasNext()) {
            ListTerm lt = i.next();
            if (lt.getTerm() == null)
                return this;
            addAnnot(lt.getTerm()); // use addAnnot to sort them
            if (lt.isTail()) {
                annots.setTail(lt.getTail());
                return this;
            }
        }
        return this;
    }

    @Override
    public boolean addAnnot(Term t) {
        if (annots == null)
            annots = new ListTermImpl();
        Iterator<ListTerm> i = annots.listTermIterator();
        while (i.hasNext()) {
            ListTerm lt = i.next();
            int c = t.compareTo(lt.getTerm());
            if (c == 0) { // equals
                return false;
            } else if (c < 0) {
                lt.insert(t);
                return true;
            }
        }
        return false;
    }

    @Override
    public Literal addAnnots(List<Term> l) {
        if (l != null)
            for (Term t : l)
                addAnnot(t);
        return this;
    }

    @Override
    public Literal addAnnots(Term ... l) {
        for (Term t : l)
            addAnnot(t);
        return this;
    }

    @Override
    public boolean delAnnot(Term t) {
        if (annots == null)
            return false;
        else
            return annots.remove(t); // TODO: use the sorted annots to reduce search (as in addAnnot)
    }

    @Override
    public void clearAnnots() {
        annots = null;
    }

    @Override
    public ListTerm getAnnots() {
        return annots;
    }

    @Override
    public boolean hasAnnot(Term t) {
        if (annots == null)
            return false;
        // annots are ordered
        Iterator<ListTerm> i = annots.listTermIterator();
        while (i.hasNext()) {
            ListTerm lt = i.next();
            int c = t.compareTo(lt.getTerm());
            if (c == 0) { // equals
                return true;
            } else if (c < 0) {
                return false;
            }
        }
        return false; //annots.contains(t);
    }

    @Override
    public Literal getAnnot(String functor) {
        if (annots == null)
            return null;
        // annots are ordered
        for (Term t: annots) {
            if (t.isLiteral()) {
                Literal l = (Literal)t;
                int c = functor.compareTo(l.getFunctor());
                if (c == 0) { // equals
                    return l;
                } else if (c < 0) {
                    return null;
                }
            }
        }
        return null;
    }


    @Override
    public boolean hasAnnot() {
        return annots != null && !annots.isEmpty();
    }

    @Override
    public boolean hasVar(VarTerm t, Unifier u) {
        if (super.hasVar(t, u))
            return true;
        if (annots != null)
            for (Term v: annots)
                if (v.hasVar(t, u))
                    return true;
        return false;
    }

    @Override
    public void countVars(Map<VarTerm, Integer> c) {
        super.countVars(c);
        if (annots != null)
            for (Term t: annots) {
                t.countVars(c);
            }
    }

    @Override
    public boolean importAnnots(Literal p) {
        boolean imported = false;
        if (p.hasAnnot()) {
            Iterator<Term> i = p.getAnnots().iterator();
            while (i.hasNext()) {
                Term t = i.next();
                // p will only contain the annots actually added (for Event)
                if (addAnnot(t.clone())) {
                    imported = true;
                } else {
                    i.remove(); // Remove what is not new from p
                }
            }
        }
        return imported;
    }

    @Override
    public boolean delAnnots(List<Term> l) {
        boolean removed = false;
        if (l != null && this.hasAnnot()) {
            for (Term t: l) {
                boolean r = delAnnot(t);
                removed = removed || r;
            }
        }
        return removed;
    }

    @Override
    public ListTerm getAnnots(String functor) {
        ListTerm ls = new ListTermImpl();
        if (annots != null) {
            ListTerm tail = ls;
            for (Term ta : annots) {
                if (ta.isLiteral()) {
                    if (((Literal)ta).getFunctor().equals(functor)) {
                        tail = tail.append(ta);
                    }
                }
            }
        }
        return ls;
    }

    @Override
    public boolean hasSubsetAnnot(Literal p) {
        if (annots == null)
            return true;
        if (hasAnnot() && !p.hasAnnot())
            return false;

        // both has annots (annots are ordered)
        Iterator<Term> i2 = p.getAnnots().iterator();
        int c = -1;
        for (Term myAnnot : annots) { // all my annots should be member of p annots
            // move i2 until it is >= myAnnot
            if (!i2.hasNext())
                return false;
            while (i2.hasNext()) {
                Term t = i2.next();
                c = myAnnot.compareTo(t);
                if (c <= 0)
                    break; // found my annot in p's annots OR my annot is not in p's annots, stop searching
            }
            if (c != 0)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasSubsetAnnot(Literal p, Unifier u) {
        if (annots == null)
            return true;
        if (!p.hasAnnot())
            return false;

        Term thisTail    = null;

        // since p's annots will be changed, clone the list (but not the terms)
        ListTerm pAnnots     = p.getAnnots().cloneLTShallow();
        VarTerm  pTail       = pAnnots.getTail();
        Term pAnnot          = null;
        ListTerm pAnnotsTail = null;

        Iterator<Term> i2 = pAnnots.iterator();
        boolean i2Reset   = false;

        Iterator<ListTerm> i1 = annots.listTermIterator(); // use this iterator to get the tail of the list
        while (i1.hasNext()) {
            ListTerm lt = i1.next();
            Term annot = lt.getTerm();
            if (annot == null)
                break;
            if (lt.isTail())
                thisTail = lt.getTail();
            if (annot.isVar() && !i2Reset) { // when we arrive to the vars in the annots of this, we need to start searching from the begin again
                i2Reset = true;
                i2 = pAnnots.iterator();
                pAnnot = null;
            }

            // search annot in p's annots
            boolean ok = false;

            while (true) {
                if (pAnnot != null && u.unifiesNoUndo(annot, pAnnot)) {
                    ok = true;
                    i2.remove();
                    pAnnot = i2.next();
                    break;
                } else if (pAnnot != null && pAnnot.compareTo(annot) > 0) {
                    break; // quite the loop, the current p annot is greater than this annot, so annot is not in p's annots
                } else if (i2.hasNext()) {
                    pAnnot = i2.next();
                } else {
                    break;
                }
            }

            // if p has a tail, add annot in p's tail
            if (!ok && pTail != null) {
                if (pAnnotsTail == null) {
                    pAnnotsTail = (ListTerm)u.get(pTail);
                    if (pAnnotsTail == null) {
                        pAnnotsTail = new ListTermImpl();
                        u.unifies(pTail, pAnnotsTail);
                        pAnnotsTail = (ListTerm)u.get(pTail);
                    }
                }
                pAnnotsTail.add(annot.clone());
                ok = true;
            }
            if (!ok)
                return false;
        }

        // if this Pred has a Tail, unify it with p remaining annots
        if (thisTail != null)
            u.unifies(thisTail, pAnnots);

        return true;
    }

    @Override
    public void addSource(Term agName) {
        if (agName != null)
            addAnnot(createSource(agName));
    }

    @Override
    public boolean delSource(Term agName) {
        if (annots != null)
            return delAnnot(createSource(agName));
        else
            return false;
    }

    public static Pred createSource(Term source) {
        Pred s;
        if (source.isGround()) {
            s = new Pred("source",1) {
                @Override
                public Term clone() {
                    return this;
                }
                @Override
                public Term capply(Unifier u) {
                    return this;
                }
                @Override
                public boolean isGround() {
                    return true;
                }
                @Override
                public Literal makeVarsAnnon() {
                    return this;
                }
                @Override
                public Literal makeVarsAnnon(Unifier un) {
                    return this;
                }
            };
        } else { // source is a var, so cannot be optimised
            s = new Pred("source",1);
        }
        s.addTerm(source);
        return s;
    }

    @Override
    public ListTerm getSources() {
        ListTerm ls = new ListTermImpl();
        if (annots != null) {
            ListTerm tail = ls;
            for (Term ta : annots) {
                if (ta.isStructure()) {
                    Structure tas = (Structure)ta;
                    if (tas.getFunctor().equals("source")) {
                        tail = tail.append(tas.getTerm(0));
                    }
                }
            }
        }
        return ls;
    }

    @Override
    public void delSources() {
        if (annots != null) {
            Iterator<Term> i = annots.iterator();
            while (i.hasNext()) {
                Term t = i.next();
                if (t.isStructure()) {
                    if (((Structure)t).getFunctor().equals("source")) {
                        i.remove();
                    }
                }
            }
        }
    }

    @Override
    public boolean hasSource() {
        if (annots != null) {
            for (Term ta : annots) {
                if (ta.isStructure()) {
                    if (((Structure)ta).getFunctor().equals("source")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasSource(Term agName) {
        if (annots != null) {
            return hasAnnot(createSource(agName));
        }
        return false;
    }


    @Override
    public Literal makeVarsAnnon(Unifier un) {
        if (annots != null) {
            ListTerm lt = annots;
            while (!lt.isEmpty()) {
                Term ta = lt.getTerm();
                if (ta.isVar())
                    lt.setTerm(varToReplace(ta, un));
                else if (ta instanceof Structure)
                    ((Structure)ta).makeVarsAnnon(un);
                if (lt.isTail() && lt.getNext().isVar()) {
                    lt.setNext(varToReplace(lt.getNext(), un));
                    break;
                }
                lt = lt.getNext();
            }
        }
        return super.makeVarsAnnon(un);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Pred) {
            final Pred p = (Pred) o;
            return super.equals(o) && this.hasSubsetAnnot(p) && p.hasSubsetAnnot(this);
        } else if (o instanceof Atom && !hasAnnot() ) { // if o is some object that extends Atom (e.g. structure), goes to super equals
            return super.equals(o);                     // consider super equals only when this has no annots
        }
        return false;
    }


    public boolean equalsAsStructure(Object p) { // this method must be in this class, do not move (I do not remember why!)
        return super.equals((Term) p);
    }

    @Override
    public int compareTo(Term t) {
        int c = super.compareTo(t);
        if (c != 0)
            return c;

        if (t.isPred()) {
            Pred tAsPred = (Pred)t;
            if (getAnnots() == null && tAsPred.getAnnots() == null) return 0;
            if (getAnnots() == null) return -1;
            if (tAsPred.getAnnots() == null) return 1;

            Iterator<Term> pai = tAsPred.getAnnots().iterator();
            for (Term a : getAnnots()) {
                c = a.compareTo(pai.next());
                if (c != 0)
                    return c;
            }

            final int ats = getAnnots().size();
            final int ots = tAsPred.getAnnots().size();
            if (ats < ots) return -1;
            if (ats > ots) return 1;
        }
        return 0;
    }

    @Override
    public Term capply(Unifier u) {
        return new Pred(this,u);
    }

    public Term clone() {
        return new Pred(this);
    }

    @Override
    public Literal cloneNS(Atom newnamespace) {
        return new Pred(newnamespace, this);
    }

    public String toStringAsTerm() {
        return super.toString();
    }

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        if (hasAnnot()) {
            Element ea = document.createElement("annotations");
            ea.appendChild(getAnnots().getAsDOM(document));
            u.appendChild(ea);
        }
        return u;
    }
}
