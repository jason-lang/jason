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

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Represents an AgentSpack plan 
    (it extends structure to be used as a term)
    
 @navassoc - label - Pred
 @navassoc - event - Trigger
 @navassoc - context - LogicalFormula
 @navassoc - body - PlanBody
 @navassoc - source - SourceInfo

 */
public class Plan extends Structure implements Cloneable, Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Term TAtomic         = ASSyntax.createAtom("atomic");
    private static final Term TBreakPoint     = ASSyntax.createAtom("breakpoint");
    private static final Term TAllUnifs       = ASSyntax.createAtom("all_unifs");
    
    private static Logger     logger          = Logger.getLogger(Plan.class.getName());
    
    private Pred              label  = null;
    private Trigger           tevent = null;
    private LogicalFormula    context;
    private PlanBody          body;
    
    private boolean isAtomic      = false;
    private boolean isAllUnifs    = false;
    private boolean hasBreakpoint = false;
    
    private boolean     isTerm = false; // it is true when the plan body is used as a term instead of an element of a plan

    // used by clone
    public Plan() {
        super("plan", 0);
    }
    
    // used by parser
    public Plan(Pred label, Trigger te, LogicalFormula ct, PlanBody bd) {
        super("plan", 0);
        tevent = te;
        tevent.setAsTriggerTerm(false);
        setLabel(label);
        setContext(ct);
        if (bd == null) {
            body = new PlanBodyImpl();
        } else {
            body = bd;
            body.setAsBodyTerm(false);
        }
    }
    
    @Override
    public int getArity() {
        return 4;
    }
    
    private static final Term noLabelAtom = new Atom("nolabel");
    
    @Override
    public Term getTerm(int i) {
        switch (i) {
        case 0: return (label == null) ? noLabelAtom : label;
        case 1: return tevent;
        case 2: return (context == null) ? Literal.LTrue : context;
        case 3: return body;
        default: return null;
        }
    }
    
    @Override
    public void setTerm(int i, Term t) {
        switch (i) {
        case 0: label   = (Pred)t; break;
        case 1: tevent  = (Trigger)t; break;
        case 2: context = (LogicalFormula)t; break;
        case 3: body    = (PlanBody)t; break;
        }
    }
    
    public void setLabel(Pred p) {
        label = p;
        if (p != null && p.hasAnnot()) {
            for (Term t: label.getAnnots()) {
                if (t.equals(TAtomic))
                    isAtomic = true;
                if (t.equals(TBreakPoint))
                    hasBreakpoint = true;
                if (t.equals(TAllUnifs))
                    isAllUnifs = true;
                // if change here, also change the clone()!
            }
        }
    }
    
    public Pred getLabel() {
        return label;
    }
    
    public void setContext(LogicalFormula le) {
        context = le;
        if (Literal.LTrue.equals(le))
            context = null;
    }
    
    public void setAsPlanTerm(boolean b) {
        isTerm = b;
    }

    /** prefer using ASSyntax.parsePlan */
    public static Plan parse(String sPlan) {
        as2j parser = new as2j(new StringReader(sPlan));
        try {
            return parser.plan();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing plan " + sPlan, e);
            return null;
        }
    }
    
    /** @deprecated use getTrigger */
    public Trigger getTriggerEvent() {
        return tevent;
    }

    public Trigger getTrigger() {
        return tevent;
    }
    
    public LogicalFormula getContext() {
        return context;
    }
    
    public PlanBody getBody() {
        return body;
    }
    
    public boolean isAtomic() {
        return isAtomic;
    }
    
    public boolean hasBreakpoint() {
        return hasBreakpoint;
    }

    public boolean isAllUnifs() {
        return isAllUnifs; 
    }
    
    /** returns an unifier if this plan is relevant for the event <i>te</i>, 
        returns null otherwise.
    */
    public Unifier isRelevant(Trigger te) {
        // annots in plan's TE must be a subset of the ones in the event!
        // (see definition of Unifier.unifies for 2 Preds)
        Unifier u = new Unifier();
        if (u.unifiesNoUndo(tevent, te))
            return u;
        else
            return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (o != null && o instanceof Plan) {
            Plan p = (Plan) o;
            if (context == null && p.context != null) return false;
            if (context != null && p.context != null && !context.equals(p.context)) return false;
            return tevent.equals(p.tevent) && body.equals(p.body);
        }
        return false;
    }
        
    public Plan capply(Unifier u) {
        Plan p = new Plan();
        if (label != null) { 
            p.label         = (Pred) label.capply(u);
            p.isAtomic      = isAtomic;
            p.hasBreakpoint = hasBreakpoint;
            p.isAllUnifs    = isAllUnifs;
        }
        
        p.tevent = tevent.capply(u);        
        if (context != null) 
            p.context = (LogicalFormula)context.capply(u);
        p.body = (PlanBody)body.capply(u);
        p.setSrcInfo(srcInfo);
        p.isTerm = isTerm;

        return p;
    }

    public Term clone() {
        Plan p = new Plan();
        if (label != null) { 
            p.label         = (Pred) label.clone();
            p.isAtomic      = isAtomic;
            p.hasBreakpoint = hasBreakpoint;
            p.isAllUnifs    = isAllUnifs;
        }
        
        p.tevent = tevent.clone();        
        if (context != null) 
            p.context = (LogicalFormula)context.clone();
        p.body = body.clonePB();
        p.setSrcInfo(srcInfo);
        p.isTerm = isTerm;

        return p;
    }

    /** used to create a plan clone in a new IM */
    public Plan cloneOnlyBody() {
        Plan p = new Plan();
        if (label != null) { 
            p.label         = label;
            p.isAtomic      = isAtomic;
            p.hasBreakpoint = hasBreakpoint;
            p.isAllUnifs    = isAllUnifs;
        }
        
        p.tevent  = tevent.clone();
        p.context = context;
        p.body    = body.clonePB();
        
        p.setSrcInfo(srcInfo);
        p.isTerm = isTerm;

        return p;
    }
    
    public String toString() {
        return toASString();
    }
    
    /** returns this plan in a string complaint with AS syntax */
    public String toASString() {
        String b, e;
        if (isTerm) {
            b = "{ "; 
            e = " }";
        } else {
            b = ""; 
            e = ".";
        }
        return b+((label == null) ? "" : "@" + label + " ") + 
               tevent + ((context == null) ? "" : " : " + context) +
               (body.isEmptyBody() ? "" : " <- " + body) +
               e;
    }
    
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("plan");
        if (label != null) {
            Element l = (Element) document.createElement("label");
            l.appendChild(new LiteralImpl(label).getAsDOM(document));
            u.appendChild(l);
        }
        u.appendChild(tevent.getAsDOM(document));
        
        if (context != null) {
            Element ec = (Element) document.createElement("context");
            ec.appendChild(context.getAsDOM(document));
            u.appendChild(ec);
        }
        
        if (!body.isEmptyBody()) {
            u.appendChild(body.getAsDOM(document));
        }
        
        return u;
    }
}
