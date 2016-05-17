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

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** 
   Represents a binary/unary logical/relational operator.
   
   @opt nodefillcolor lightgoldenrodyellow
 
   @navassoc - left  - Term
   @navassoc - right - Term
    
 */
public abstract class BinaryStructure extends Structure {

    /** Constructor for binary operator */
    public BinaryStructure(Term t1, String id, Term t2) {
        super(id,2);
        addTerm(t1);
        addTerm(t2);
        if (t1.getSrcInfo() != null)
            srcInfo = t1.getSrcInfo();
        else
            srcInfo = t2.getSrcInfo();
    }

    /** Constructor for unary operator */
    public BinaryStructure(String id, Term arg) {
        super(id,1);
        addTerm( arg );
        srcInfo = arg.getSrcInfo();
    }
    
    /** gets the LHS of this operation */
    public Term getLHS() {
        return getTerm(0);
    }
    
    /** gets the RHS of this operation */
    public Term getRHS() {
        return getTerm(1);
    }

    @Override
    public String toString() {
        if (isUnary()) {
            return getFunctor()+"("+getTerm(0)+")";
        } else {
            return "("+getTerm(0)+getFunctor()+getTerm(1)+")";
        }
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("expression");
        u.setAttribute("operator", getFunctor().toString());
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
