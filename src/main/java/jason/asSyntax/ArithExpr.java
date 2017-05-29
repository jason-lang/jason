package jason.asSyntax;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.NoValueException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

/**
  Represents and solve arithmetic expressions like "10 + 30".

  @navassoc - op - ArithmeticOp
 */
public class ArithExpr extends ArithFunctionTerm implements NumberTerm {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(ArithExpr.class.getName());

    public enum ArithmeticOp {
        none {
            double eval(double x, double y) {
                return 0;
            }

            public String toString() {
                return "";
            }
        },
        plus {
            double eval(double x, double y) {
                return x + y;
            }

            public String toString() {
                return "+";
            }
        },
        minus {
            double eval(double x, double y) {
                return x - y;
            }

            public String toString() {
                return "-";
            }
        },
        times {
            double eval(double x, double y) {
                return x * y;
            }

            public String toString() {
                return "*";
            }
        },
        div {
            double eval(double x, double y) {
                return x / y;
            }

            public String toString() {
                return "/";
            }
        },
        mod {
            double eval(double x, double y) {
                return x % y;
            }

            public String toString() {
                return " mod ";
            }
        },
        pow {
            double eval(double x, double y) {
                return Math.pow(x, y);
            }

            public String toString() {
                return "**";
            }
        },
        intdiv {
            double eval(double x, double y) {
                return (int) x / (int) y;
            }

            public String toString() {
                return " div ";
            }
        };

        abstract double eval(double x, double y);
    }

    private ArithmeticOp  op = ArithmeticOp.none;

    public ArithExpr(NumberTerm t1, ArithmeticOp oper, NumberTerm t2) {
        super(oper.toString(),2);
        addTerm(t1);
        addTerm(t2);
        op = oper;
        if (t1.getSrcInfo() != null)
            srcInfo = t1.getSrcInfo();
        else
            srcInfo = t2.getSrcInfo();
    }

    public ArithExpr(ArithmeticOp oper, NumberTerm t1) {
        super(oper.toString(),1);
        addTerm(t1);
        op = oper;
        srcInfo = t1.getSrcInfo();
    }

    private ArithExpr(ArithExpr ae) { // for clone
        super(ae);
        op = ae.op;
    }

    /** returns some Term that can be evaluated as Number */
    public static NumberTerm parseExpr(String sExpr) {
        return parseExpr(null, sExpr);
    }

    /** returns some Term that can be evaluated as Number */
    public static NumberTerm parseExpr(Agent ag, String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        if (ag != null)
            parser.setAg(ag);
        try {
            return (NumberTerm) parser.arithm_expr();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing expression " + sExpr, e);
            return null;
        }
    }

    @Override
    public Term capply(Unifier u) {
        try {
            double l = ((NumberTerm)getTerm(0).capply(u)).solve();
            if (isUnary()) {
                if (op == ArithmeticOp.minus) {
                    value = new NumberTermImpl(-l);
                } else {
                    value = new NumberTermImpl(l);
                }
            } else {
                double r = ((NumberTerm)getTerm(1).capply(u)).solve();
                value = new NumberTermImpl(op.eval(l, r));
            }
            return value;
        } catch (ClassCastException e) {
            logger.warning("The value of "+this+" is not a number! Unifier = "+u+". Code: "+getSrcInfo());
            return new NumberTermImpl(Double.NaN);
        } catch (NoValueException e) {
            return clone();
        }
    }

    public boolean checkArity(int a) {
        return a == 1 || a == 2;
    }

    /** make a hard copy of the terms */
    public NumberTerm clone() {
        return new ArithExpr(this);
    }

    /** gets the Operation of this Expression */
    public ArithmeticOp getOp() {
        return op;
    }

    /** gets the LHS of this Expression */
    public NumberTerm getLHS() {
        return (NumberTerm)getTerm(0);
    }

    /** gets the RHS of this Expression */
    public NumberTerm getRHS() {
        return (NumberTerm)getTerm(1);
    }

    @Override
    public String toString() {
        if (isUnary()) {
            return "(" + op + getTerm(0) + ")";
        } else {
            return "(" + getTerm(0) + op + getTerm(1) + ")";
        }
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("expression");
        u.setAttribute("type", "arithmetic");
        u.setAttribute("operator", op.toString());
        if (isUnary()) {
            Element r = (Element) document.createElement("right");
            r.appendChild(getTerm(0).getAsDOM(document)); // put the left argument indeed!
            u.appendChild(r);
        } else {
            Element l = (Element) document.createElement("left");
            l.appendChild(getTerm(0).getAsDOM(document));
            u.appendChild(l);
            Element r = (Element) document.createElement("right");
            r.appendChild(getTerm(1).getAsDOM(document));
            u.appendChild(r);
        }
        return u;
    }
}
