package jason.asSyntax;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 *  Represents a plan body item (achieve, test, action, ...) and its successors.
 * 
 *  A plan body like <code>a1; ?t; !g</code> is represented by the following structure
 *  <code>(a1, (?t, (!g)))</code>.
 *  
 *  
 *  @navassoc - next - PlanBody
 *  @navassoc - type - PlanBody.BodyType
 *  
 *  @author Jomi  
 */
public class PlanBodyImpl extends Structure implements PlanBody, Iterable<PlanBody> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PlanBodyImpl.class.getName());

    public static final String BODY_PLAN_FUNCTOR = ";";

    private Term        term     = null; 
    private PlanBody    next     = null;
    private BodyType    formType = BodyType.none;
    
    private boolean     isTerm = false; // it is true when the plan body is used as a term instead of an element of a plan
    
    /** constructor for empty plan body */
    public PlanBodyImpl() {
        super(BODY_PLAN_FUNCTOR, 0);
    }
    
    public PlanBodyImpl(boolean planTerm) {
        this();
        setAsBodyTerm(planTerm);
    }

    public PlanBodyImpl(BodyType t, Term b) {
        this(t,b,false);        
    }
    
    public PlanBodyImpl(BodyType t, Term b, boolean planTerm) {
        this(planTerm);
        formType = t;
        if (b != null) { 
            srcInfo = b.getSrcInfo();

            /*
            // add source(self) in some commands (it is preferred to do this at compile time than runtime) 
            // DOES NOT work with variables (see the case of kqmlPlans.asl and the bug reported by Alexandro)
            if (b instanceof Literal) {
                switch (formType) {
                case achieve:
                case achieveNF:
                case addBel:
                case addBelBegin:
                case addBelEnd:
                case addBelNewFocus:
                case delBel:
                case delAddBel:
                    
                    Literal l = (Literal)b;
                    l = l.forceFullLiteralImpl();
                    if (!l.hasSource()) { // do not add source(self) in case the programmer set the source
                        l.addAnnot(BeliefBase.TSelf);
                    }
                    b = l;

                default:
                    break;
                }
            }
            */
        }
        term = b;
    }

    public void setBodyNext(PlanBody next) {
        this.next = next;
    }
    public PlanBody getBodyNext() {
        return next;
    }

    public boolean isEmptyBody() {
        return term == null;
    }
    
    public BodyType getBodyType() {
        return formType;
    }
    public void setBodyType(BodyType bt) {
        formType = bt;
    }
    
    public Term getBodyTerm() {
        return term;
    }
    
    public void setBodyTerm(Term t) {
        term = t;
    }
    
    public boolean isBodyTerm() {
        return isTerm;
    }
    
    @Override
    public boolean isAtom() {
        return false;
    }
    
    public void setAsBodyTerm(boolean b) {
        if (b != isTerm) {
            isTerm = b;
            if (getBodyNext() != null) // goes deep only if changed
                getBodyNext().setAsBodyTerm(b);
        }
    }
    
    @Override
    public boolean isPlanBody() {
        return true;
    }
    
    public Iterator<PlanBody> iterator() {
        return new Iterator<PlanBody>() {
            PlanBody current = PlanBodyImpl.this;
            public boolean hasNext() {
                return current != null && current.getBodyTerm() != null; 
            }
            public PlanBody next() {
                PlanBody r = current;
                if (current != null)
                    current = current.getBodyNext();
                return r;
            }
            public void remove() { }
        };
    }

    // Override some structure methods to work with unification/equals
    @Override
    public int getArity() {
        if (term == null)
            return 0;
        else if (next == null)
            return 1;
        else
            return 2;
    }

    @Override
    public Term getTerm(int i) {
        if (i == 0) 
            return term;
        if (i == 1) {
            if (next != null && next.getBodyTerm().isVar() && next.getBodyNext() == null) 
                // if next is the last VAR, return that var
                return next.getBodyTerm();
            else
                return next;
        }
        return null;
    }

    @Override
    public void setTerm(int i, Term t) {
        if (i == 0) term = t;
        if (i == 1) { // (NIDE) if next is the last VAR...
            if (next != null && next.getBodyTerm().isVar() && next.getBodyNext() == null) 
                next.setBodyTerm(t);
            else
                System.out.println("Should not setTerm(1) of body literal!");
        }
    }
    
    /*
    private boolean applyHead(Unifier u) {
        if (term != null && term.apply(u)) {
            if (term.isPlanBody()) { // we cannot have "inner" body literals
                PlanBody baknext = next;
                formType = ((PlanBody)term).getBodyType();
                next     = ((PlanBody)term).getBodyNext();
                term     = ((PlanBody)term).getBodyTerm();
                if (baknext != null) {
                    baknext.apply(u);
                    getLastBody().add(baknext);
                }
            }
            return true;
        }        
        return false;
    }
    */

    /*
    @Override
    public boolean apply(Unifier u) {
        boolean ok = next != null && next.apply(u);
        
        if (applyHead(u))
            ok = true;

        if (ok)
            resetHashCodeCache();

        return ok;
    }
    */

    @Override
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un) {
        logger.log(Level.WARNING, "PlanBodyImpl cannot be used for logical consequence!", new Exception());
        return LogExpr.EMPTY_UNIF_LIST.iterator();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof PlanBody) {
            PlanBody b = (PlanBody)o;
            return formType == b.getBodyType() && super.equals(o);
        }
        return false;
    }

    @Override
    public int calcHashCode() {
        return formType.hashCode() + super.calcHashCode();
    }

    /** clone the plan body and adds it in the end of this plan */
    public boolean add(PlanBody bl) {
        if (bl == null) // (NIDE) if bl is empty, do nothing
            return true;
        if (term == null) {
            bl = bl.clonePB();
            swap(bl);
            this.next = bl.getBodyNext();
        } else if (next == null) {
            next = bl;
        } else { 
            next.add(bl);
        }
        return true;
    }

    public PlanBody getLastBody() {
        if (next == null)
            return this;
        else
            return next.getLastBody();
    }
    
    public boolean add(int index, PlanBody bl) {
        if (index == 0) {
            PlanBody newpb = new PlanBodyImpl(this.formType, this.term);
            newpb.setBodyNext(next);
            swap(bl);
            this.next = bl.getBodyNext();
            this.getLastBody().setBodyNext(newpb);
        } else if (next != null) { 
            next.add(index - 1, bl);
        } else {
            next = bl;
        }
        return true;
    }

    public Term removeBody(int index) {
        if (index == 0) {
            Term oldvalue = term;
            if (next == null) {
                term = null; // becomes an empty
            } else {
                swap(next); // get values of text
                next = next.getBodyNext();
            }
            return oldvalue;
        } else { 
            return next.removeBody(index - 1);
        }
    }

    public int getPlanSize() {
        if (term == null) 
            return 0;
        else if (next == null)
            return 1;
        else
            return next.getPlanSize() + 1;
    }

    private void swap(PlanBody bl) {
        BodyType bt = this.formType;
        this.formType = bl.getBodyType();
        bl.setBodyType(bt);

        Term l = this.term;
        this.term = bl.getBodyTerm();
        bl.setBodyTerm(l);
    }

    @Override
    public PlanBody capply(Unifier u) {
        //System.out.println(this+" with "+u);
        PlanBodyImpl c;
        if (term == null) { // (NIDE) must copy c.isTerm even if cloning empty plan
            c = new PlanBodyImpl();
            c.isTerm = isTerm;
        } else {
            c = new PlanBodyImpl(formType, term.capply(u), isTerm);
            if (c.term.isPlanBody()) { // we cannot have "inner" body literals
                c.formType = ((PlanBody)c.term).getBodyType();
                c.next     = ((PlanBody)c.term).getBodyNext();
                c.term     = ((PlanBody)c.term).getBodyTerm();
            }
        }
        
        if (next != null)
            c.add((PlanBody)next.capply(u));

        //System.out.println(this+" = "+c+" using "+u+" term="+c.term+"/"+term.capply(u));
        return c;
    }

    public PlanBody clone() {
        PlanBodyImpl c;    
        if (term == null)  // (NIDE) must copy c.isTerm even if cloning empty plan
            c = new PlanBodyImpl();
        else 
            c = new PlanBodyImpl(formType, term.clone(), isTerm);
        c.isTerm = isTerm;
        if (next != null)
            c.setBodyNext(getBodyNext().clonePB());
        return c;
    }
    
    public PlanBody clonePB() {
        return clone();
    }
    
    public String toString() {
        if (term == null) {
            return isTerm ? "{ }" : ""; // NIDE
        } else {
            StringBuilder out = new StringBuilder();
            if (isTerm)
                out.append("{ ");
            PlanBody pb = this;
            while (pb != null) {
                if (pb.getBodyTerm() != null) {
                    out.append(pb.getBodyType());
                    out.append(pb.getBodyTerm());
                }
                pb = pb.getBodyNext();
                if (pb != null)
                    out.append("; ");
            }
            if (isTerm) 
                out.append(" }"); 
            return out.toString();
        }
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element eb = (Element) document.createElement("body");
        PlanBody bl = this;
        while (bl != null && !bl.isEmptyBody()) {
            Element u = (Element) document.createElement("body-literal");
            if (bl.getBodyType().toString().length() > 0) {
                u.setAttribute("type", bl.getBodyType().toString());
            }
            u.appendChild( ((Structure)bl.getBodyTerm()).getAsDOM(document));
            eb.appendChild(u);
            
            bl = bl.getBodyNext();
        }
        return eb;
    }
}
