// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

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
