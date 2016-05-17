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

import jason.asSemantics.Unifier;



/**
 * A term with recursion (cyclic), created by code like X = f(X).
 */
public class CyclicTerm extends LiteralImpl  {

    private static final long serialVersionUID = 1L;
    
    private VarTerm cyclicVar = null;

    /** creates a positive literal */
    public CyclicTerm(Literal t, VarTerm v) {
        super(t);
        cyclicVar = v;
    }

    public CyclicTerm(Literal t, VarTerm v, Unifier u) {
        super(t,u);
        cyclicVar = v;
    }

    public VarTerm getCyclicVar() {
        return cyclicVar;
    }
    
    @Override
    public boolean isCyclicTerm() {
        return true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof CyclicTerm) {
            final CyclicTerm l = (CyclicTerm) o;
            return super.equals(l);
        }
        return false;
    }

    @Override
    public Literal makeVarsAnnon(Unifier u) {
        super.makeVarsAnnon(u);
        VarTerm v = u.deref(cyclicVar);
        if (v != null)
            cyclicVar = v;
        return this;
    }


    /*
    @Override
    public boolean apply(Unifier u) {
        Term v = u.remove(cyclicVar);
        boolean b = super.apply(u);
        if (v != null)
            u.bind(cyclicVar, v);
        return b;
    }
    */
    
    @Override
    public Term capply(Unifier u) {
        Term v = u.remove(cyclicVar);
        Term r = new CyclicTerm(this, (VarTerm)cyclicVar.clone(), u);
        if (v != null)
            u.bind(cyclicVar, v);
        return r; 
    }

    public Term clone() {
        return new CyclicTerm(this, (VarTerm)cyclicVar.clone());
    }
    
    @Override
    protected int calcHashCode() {
        return super.calcHashCode() + cyclicVar.calcHashCode();
    }

    public String toString() {
        return "..."+super.toString()+"/"+cyclicVar;
    }

    @Override
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        u.setAttribute("cyclic-var", cyclicVar.toString());
        return u;
    }
}
