package jason.asSyntax;

import jason.asSemantics.Unifier;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
     A rule is a Literal (head) with a body, as in "a :- b &amp; c".

     @navassoc - body - LogicalFormula
 */
public class Rule extends LiteralImpl {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Rule.class.getName());

    private LogicalFormula body   = null;

    private boolean     isTerm = false; // it is true when the rule is used as a term ( { p :- q } )

    public Rule(Literal head, LogicalFormula body) {
        super(head);
        if (head.isRule()) {
            logger.log(Level.SEVERE,"The rule head ("+head+") is a rule!", new Exception());
        } else if (isInternalAction()) {
            logger.log(Level.SEVERE,"The rule head ("+head+") can not be an internal action!", new Exception());
        } else if (head == LTrue || head == LFalse) {
            logger.log(Level.SEVERE,"The rule head ("+head+") can not be a true or false!", new Exception());
        }
        this.body = body;
    }

    public Rule(Rule r, Unifier u) {
        super(r,u);
        this.isTerm = r.isTerm;
        body = (LogicalFormula)r.body.capply(u);
        predicateIndicatorCache = null;
    }


    @Override
    public boolean isRule() {
        return true;
    }

    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public boolean isGround() {
        return false;
    }

    public void setAsTerm(boolean b) {
        isTerm = b;
    }

    public boolean isTerm() {
        return isTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Rule) {
            Rule r = (Rule) o;
            return super.equals(o) && body.equals(r.body);
        }
        return false;
    }

    @Override
    protected int calcHashCode() {
        return super.calcHashCode() + body.hashCode();
    }

    public LogicalFormula getBody() {
        return body;
    }

    public Literal getHead() {
        return new LiteralImpl(this);
    }

    @Override
    public Literal makeVarsAnnon(Unifier un) {
        if (body instanceof Literal)
            ((Literal)body).makeVarsAnnon(un);
        return super.makeVarsAnnon(un);
    }

    @Override
    public Term capply(Unifier u) {
        return new Rule(this,u);
    }

    public Rule clone() {
        Rule r = new Rule((Literal)super.clone(), (LogicalFormula)body.clone());
        r.predicateIndicatorCache = null;
        r.resetHashCodeCache();
        r.isTerm = this.isTerm;
        return r;
    }

    public Literal headClone() {
        return (Literal)super.clone();
    }

    public Literal headCApply(Unifier u) {
        return (Literal)super.capply(u);
    }

    public String toString() {
        if (isTerm())
            return "{ " + super.toString() + " :- " + body + " }";
        else
            return super.toString() + " :- " + body;
    }

    @Override
    public boolean hasVar(VarTerm t, Unifier u) {
        if (super.hasVar(t, u)) return true;
        return body.hasVar(t, u);
    }

    public void countVars(Map<VarTerm, Integer> c) {
        super.countVars(c);
        body.countVars(c);
    }

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("rule");

        Element h = (Element) document.createElement("head");
        h.appendChild(super.getAsDOM(document));

        Element b = (Element) document.createElement("context");
        b.appendChild(body.getAsDOM(document));

        u.appendChild(h);
        u.appendChild(b);
        return u;
    }
}
