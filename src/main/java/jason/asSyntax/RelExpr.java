package jason.asSyntax;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a relational expression like 10 > 20.
 *
 * When the operator is <b>=..</b>, the first argument is a literal and the
 * second as list, e.g.:
 * <code>
 * Literal =.. [functor, list of terms, list of annots]
 * </code>
 * Examples:
 * <ul>
 * <li> X =.. [~p, [t1, t2], [a1,a2]]<br>
 *      X is ~p(t1,t2)[a1,a2]
 * <li> ~p(t1,t2)[a1,a2] =.. X<br>
 *      X is [~p, [t1, t2], [a1,a2]]
 * </ul>
 *
 * @navassoc - op - RelationalOp
 *
 * @author Jomi
 */
public class RelExpr extends BinaryStructure implements LogicalFormula {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(RelExpr.class.getName());

    public enum RelationalOp {
        none   { public String toString() {
                return "";
            }
        },
        gt     { public String toString() {
                return " > ";
            }
        },
        gte    { public String toString() {
                return " >= ";
            }
        },
        lt     { public String toString() {
                return " < ";
            }
        },
        lte    { public String toString() {
                return " <= ";
            }
        },
        eq     { public String toString() {
                return " == ";
            }
        },
        dif    { public String toString() {
                return " \\== ";
            }
        },
        unify          { public String toString() {
                return " = ";
            }
        },
        literalBuilder { public String toString() {
                return " =.. ";
            }
        };
    }

    private RelationalOp op = RelationalOp.none;

    public RelExpr(Term t1, RelationalOp oper, Term t2) {
        super(t1,oper.toString(),t2);
        op = oper;
    }

    public Iterator<Unifier> logicalConsequence(final Agent ag, Unifier un) {
        Term xp = getTerm(0).capply(un);
        Term yp = getTerm(1).capply(un);

        switch (op) {

        case none:
            break;

        case gt :
            if (xp.compareTo(yp)  >  0) return LogExpr.createUnifIterator(un);
            break;
        case gte:
            if (xp.compareTo(yp)  >= 0) return LogExpr.createUnifIterator(un);
            break;
        case lt :
            if (xp.compareTo(yp)  <  0) return LogExpr.createUnifIterator(un);
            break;
        case lte:
            if (xp.compareTo(yp)  <= 0) return LogExpr.createUnifIterator(un);
            break;
        case eq :
            if (xp.equals(yp))          return LogExpr.createUnifIterator(un);
            break;
        case dif:
            if (!xp.equals(yp))         return LogExpr.createUnifIterator(un);
            break;
        case unify:
            if (un.unifies(xp,yp))    return LogExpr.createUnifIterator(un);
            break;

        case literalBuilder:
            try {
                Literal  p = (Literal)xp;  // lhs clone
                ListTerm l = (ListTerm)yp; // rhs clone
                //logger.info(p+" test "+l+" un="+un);

                // both are not vars, using normal unification
                if (!p.isVar() && !l.isVar()) {
                    ListTerm palt = p.getAsListOfTerms();
                    if (l.size() == 3) // list without name space
                        palt = palt.getNext();
                    if (un.unifies(palt, l)) {
                        return LogExpr.createUnifIterator(un);
                    }
                } else {

                    // first is var, second is list, var is assigned to l transformed in literal
                    if (p.isVar() && l.isList()) {
                        if (un.unifies(p, Literal.newFromListOfTerms(l)))
                            return LogExpr.createUnifIterator(un);
                        else
                            LogExpr.EMPTY_UNIF_LIST.iterator();

                    }

                    // first is literal, second is var, var is assigned to l transformed in list
                    if (p.isLiteral() && l.isVar()) {
                        if (un.unifies(p.getAsListOfTerms(), l))
                            return LogExpr.createUnifIterator(un);
                        else
                            LogExpr.EMPTY_UNIF_LIST.iterator();
                    }

                    // both are vars, error
                    logger.log(Level.SEVERE, "Both arguments of "+getTerm(0)+" =.. "+getTerm(1)+" are variables!");
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "The arguments of operator =.. are not Literal and List.", e);
            }
            break;
        }

        return LogExpr.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }

    /** returns some LogicalFormula that can be evaluated */
    public static LogicalFormula parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (LogicalFormula)parser.rel_expr();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing expression "+sExpr,e);
        }
        return null;
    }

    @Override
    public Term capply(Unifier u) {
        return new RelExpr(getTerm(0).capply(u), op, getTerm(1).capply(u));
    }

    /** make a hard copy of the terms */
    public LogicalFormula clone() {
        return new RelExpr(getTerm(0).clone(), op, getTerm(1).clone());
    }

    @Override
    public Literal cloneNS(Atom newnamespace) {
        return new RelExpr(getTerm(0).cloneNS(newnamespace), op, getTerm(1).cloneNS(newnamespace));
    }

    /** gets the Operation of this Expression */
    public RelationalOp getOp() {
        return op;
    }
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        u.setAttribute("type","relational");
        return u;
    }
}
