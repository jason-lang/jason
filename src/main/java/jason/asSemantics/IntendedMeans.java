//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------


package jason.asSemantics;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IntendedMeans implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Unifier  unif = null;
    protected PlanBody planBody;
    protected Plan     plan;
    private   Trigger  trigger; // the trigger which created this IM
    
    protected Unifier  renamedVars = null;
        
    public IntendedMeans(Option opt, Trigger te) {
        plan     = opt.getPlan();
        planBody = plan.getBody(); 
        unif     = opt.getUnifier();
        
        if (te == null) {
            trigger = plan.getTrigger().capply(unif);
        } else {
            trigger = te.capply(unif);
        }
    }
    
    // used by clone
    private IntendedMeans() {  }

    /** removes the current action of the IM and returns the term of the body */
    public Term removeCurrentStep() {
        if (isFinished()) {
            return null;
        } else {
            Term r = planBody.getBodyTerm();
            planBody = planBody.getBodyNext();
            return r;
        }
    }

    public PlanBody getCurrentStep() {
        return planBody;
    }

    // used by if/for/loop internal actions
    public PlanBody insertAsNextStep(PlanBody pb2add) {
        planBody = new PlanBodyImpl(planBody.getBodyType(), planBody.getBodyTerm());
        planBody.setBodyNext(pb2add);
        return planBody; 
    }
    
    public Plan getPlan() {
        return plan;
    }
    
    public void setUnif(Unifier unif) {
        this.unif = unif;
    }
    
    public Unifier getUnif() {
        return unif;
    }

    /** gets the trigger event that caused the creation of this IM */
    public Trigger getTrigger() {
        return trigger;
    }
    public void setTrigger(Trigger tr) {
        trigger = tr;
    }

    public boolean isAtomic() {
        return plan != null && plan.isAtomic();
    }
    
    public boolean isFinished() {
        return planBody == null || planBody.isEmptyBody();
    }
    
    public boolean isGoalAdd() {
        return trigger.isAddition() && trigger.isGoal();
    }

    public Object clone() {
        IntendedMeans c = new IntendedMeans();
        c.unif     = this.unif.clone();
        if (this.planBody != null)
            c.planBody = this.planBody.clonePB();
        c.trigger  = this.trigger.clone(); 
        c.plan     = this.plan;
        return c;
    }
    
    public String toString() {
        return trigger + " <- ... " + planBody + " / " + unif;
    }

    public Term getAsTerm() {
        if (planBody instanceof PlanBodyImpl || planBody == null) {
            // TODO: use same replacements (Var -> Unnamed var) for the plan and for the unifier
            PlanBody bd;
            if (planBody == null) { // (NIDE) in case we must convert empty plan to Term
                bd = new PlanBodyImpl();
            } else {
                bd = (PlanBody)((PlanBodyImpl)planBody.clone()).makeVarsAnnon();
            }
            bd.setAsBodyTerm(true);
            return ASSyntax.createStructure("im", ASSyntax.createString(plan.getLabel()), bd, unif.getAsTerm());
        } else {
            return ASSyntax.createAtom("noimplementedforclass"+planBody.getClass().getSimpleName());
        }
    }
    
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element eim = (Element) document.createElement("intended-means");
        eim.setAttribute("trigger", trigger.toString());
        if (planBody != null) {
            eim.appendChild(planBody.getAsDOM(document));
        }
        if (unif != null && unif.size() > 0) {
            eim.appendChild(unif.getAsDOM(document));
        }
        return eim;
    }

}
