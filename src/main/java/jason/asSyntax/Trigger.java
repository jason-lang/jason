package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.bb.BeliefBase;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
  Represents an AgentSpeak trigger (like +!g, +p, ...).

  It is composed by:
     an operator (+ or -);
     a type (<empty>, !, or ?);
     a literal

  (it extends structure to be used as a term)

  @opt attributes
  @navassoc - literal  - Literal
  @navassoc - operator - TEOperator
  @navassoc - type     - TEType
*/
public class Trigger extends Structure implements Cloneable {

    private static Logger logger = Logger.getLogger(Trigger.class.getName());

    public enum TEOperator {
        add        { public String toString() {
                return "+";
            }
        },
        del        { public String toString() {
                return "-";
            }
        },
        goalState  { public String toString() {
                return "^";
            }
        },
    };

    public enum TEType {
        belief  { public String toString() {
                return "";
            }
        },
        achieve { public String toString() {
                return "!";
            }
        },
        test    { public String toString() {
                return "?";
            }
        }
    };


    private TEOperator operator = TEOperator.add;
    private TEType     type     = TEType.belief;
    private Literal    literal;

    private boolean     isTerm = false; // it is true when the plan body is used as a term instead of an element of a plan

    public Trigger(TEOperator op, TEType t, Literal l) {
        super("te", 0);
        literal = l;
        type    = t;
        setTrigOp(op);
        setSrcInfo(l.getSrcInfo());
    }

    /** prefer to use ASSyntax.parseTrigger */
    public static Trigger parseTrigger(String sTe) {
        as2j parser = new as2j(new StringReader(sTe));
        try {
            return parser.trigger();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing trigger" + sTe,e);
            return null;
        }
    }

    // override some structure methods
    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Term getTerm(int i) {
        switch (i) {
        case 0:
            return new StringTermImpl(operator.toString() + type.toString());
        case 1:
            return literal;
        default:
            return null;
        }
    }

    @Override
    public void setTerm(int i, Term t) {
        switch (i) {
        case 0:
            logger.warning("setTerm(i,t) for i=0 -- the operator -- , IS NOT IMPLEMENTED YET!!!");
            break;
        case  1:
            literal = (Literal)t;
            break;
        }
    }

    public void setTrigOp(TEOperator op) {
        operator = op;
        predicateIndicatorCache  = null;
    }


    public boolean sameType(Trigger e) {
        return operator == e.operator && type == e.type;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Trigger) {
            Trigger t = (Trigger) o;
            return (operator == t.operator && type == t.type && literal.equals(t.getLiteral()));
        }
        return false;
    }

    public boolean isAchvGoal() {
        return type == TEType.achieve;
    }

    public boolean isGoal() {
        return type == TEType.achieve || type == TEType.test;
    }

    public boolean isMetaEvent() {
        return operator == TEOperator.goalState;
    }

    public TEOperator getOperator() {
        return operator;
    }

    public TEType getType() {
        return type;
    }

    public boolean isAddition() {
        return operator == TEOperator.add;
    }

    public Trigger clone() {
        Trigger c = new Trigger(operator, type, literal.copy());
        c.predicateIndicatorCache = this.predicateIndicatorCache;
        c.isTerm = isTerm;
        return c;
    }

    @Override
    public Trigger capply(Unifier u) {
        Trigger c = new Trigger(operator, type, (Literal)literal.capply(u));
        c.predicateIndicatorCache = this.predicateIndicatorCache;
        c.isTerm = isTerm;
        return c;
    }


    /** return [+|-][!|?] super.getPredicateIndicator */
    @Override
    public PredicateIndicator getPredicateIndicator() {
        if (predicateIndicatorCache == null) {
            predicateIndicatorCache = new PredicateIndicator(literal.getNS(), operator.toString() + type + literal.getFunctor(), literal.getArity());
        }
        return predicateIndicatorCache;
    }

    /*public boolean apply(Unifier u) {
        return literal.apply(u);
    }*/

    public Literal getLiteral() {
        return literal;
    }

    public void setLiteral(Literal literal) {
        this.literal = literal;
        predicateIndicatorCache = null;
    }

    public void setAsTriggerTerm(boolean b) {
        isTerm = b;
    }

    public String toString() {
        String b, e;
        if (isTerm) {
            b = "{ ";
            e = " }";
        } else {
            b = "";
            e = "";
        }
        return b + operator+ type + literal + e;
    }

    /** try to convert the term t into a trigger, in case t is a trigger term, a string that can be parsed to a trigger, a var with value trigger, .... */
    public static Trigger tryToGetTrigger(Term t) throws ParseException {
        if (t instanceof Trigger) {
            return (Trigger)t;
        }
        if (t.isPlanBody()) {
            PlanBody p = (PlanBody)t;
            if (p.getPlanSize() == 1) {
                TEOperator op = null;
                if (p.getBodyType() == BodyType.addBel)
                    op = TEOperator.add;
                else if (p.getBodyType() == BodyType.delBel)
                    op = TEOperator.del;
                if (op != null) {
                    Literal l = (Literal)p.getBodyTerm().clone();
                    l.delAnnot(BeliefBase.TSelf); // remove the eventual auto added annotation of source
                    return new Trigger(op, TEType.belief, l);

                }
            }
        }
        if (t.isString()) {
            return ASSyntax.parseTrigger(((StringTerm)t).getString());
        }
        return null;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element e = (Element) document.createElement("trigger");
        e.setAttribute("operator", operator.toString());
        e.setAttribute("type", type.toString());
        e.appendChild(literal.getAsDOM(document));
        return e;
    }

}
