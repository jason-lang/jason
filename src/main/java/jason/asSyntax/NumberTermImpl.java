package jason.asSyntax;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Immutable class that implements a term that represents a number */
public final class NumberTermImpl extends DefaultTerm implements NumberTerm {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(NumberTermImpl.class.getName());

    private final double value;

    public NumberTermImpl() {
        super();
        value = 0;
    }

    /** @deprecated prefer to use ASSyntax.parseNumber */
    public NumberTermImpl(String sn) {
        double t = 0;
        try {
            t = Double.parseDouble(sn);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error setting number term value from "+sn,e);
        }
        value = t;
    }

    public NumberTermImpl(double vl) {
        value = vl;
    }

    public NumberTermImpl(NumberTermImpl t) {
        value   = t.value;
        srcInfo = t.srcInfo;
    }

    public double solve() {
        return value;
    }

    public NumberTerm clone() {
        return this;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (o != null && o instanceof Term && ((Term)o).isNumeric() && !((Term)o).isArithExpr()) {
            NumberTerm st = (NumberTerm)o;
            try {
                return solve() == st.solve();
            } catch (Exception e) { }
        }
        return false;
    }

    @Override
    protected int calcHashCode() {
        return 37 * (int)value;
    }

    @Override
    public int compareTo(Term o) {
        if (o instanceof VarTerm) {
            return o.compareTo(this) * -1;
        }
        if (o instanceof NumberTermImpl) {
            NumberTermImpl st = (NumberTermImpl)o;
            if (value > st.value) return 1;
            if (value < st.value) return -1;
            return 0;
        }
        return -1;
    }

    public String toString() {
        long r = Math.round(value);
        if (value == (double)r) {
            return String.valueOf(r);
        } else {
            return String.valueOf(value);
        }
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("number-term");
        u.appendChild(document.createTextNode(toString()));
        return u;
    }
}
