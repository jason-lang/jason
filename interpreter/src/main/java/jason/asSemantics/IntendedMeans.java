package jason.asSemantics;

import java.io.Serializable;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.util.ToDOM;

public class IntendedMeans implements Serializable, ToDOM {

    private static final long serialVersionUID = 1L;

    protected Unifier  unif = null;
    protected PlanBody planBody;
    protected Plan     plan;
    private   Trigger  trigger; // the trigger which created this IM

    protected Unifier  renamedVars = null;

    protected Unifier  triggerUnif   = null; // unif when the IM was created (used to check goal condition and g-plan scope vars)

    public IntendedMeans(Option opt, Trigger te) {
        plan     = opt.getPlan();
        planBody = plan.getBody();
        unif     = opt.getUnifier();

        if (te == null) {
            trigger = plan.getTrigger().capply(unif);
        } else {
            trigger = te.capply(unif);
        }

        triggerUnif = unif.clone();
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

    public boolean isSatisfied(Agent ag) {
        LogicalFormula goalCondition = getPlan().getGoalCondition();
        if (goalCondition == null) {
            return false;
        } else {
            Iterator<Unifier> iun = goalCondition.logicalConsequence(ag, triggerUnif);
            return iun != null && iun.hasNext();
        }
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
        if (this.triggerUnif != null)
            c.triggerUnif = this.triggerUnif.clone();
        c.plan     = this.plan;
        return c;
    }

    public String toString() {
        return trigger + " <- " + (planBody == null ? "." : "... " + planBody) + " / " + unif;
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
            Trigger te = getTrigger().clone();
            te.setAsTriggerTerm(true);
            Literal label = plan.getLabel().copy();
            label.addSourceInfoAsAnnots(plan.getSrcInfo());
            return ASSyntax.createStructure("im", label, te, bd, unif.getAsTerm());
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
