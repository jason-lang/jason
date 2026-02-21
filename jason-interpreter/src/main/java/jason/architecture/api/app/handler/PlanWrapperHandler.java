package jason.architecture.api.app.handler;

import jason.architecture.api.app.model.state.plan.PlanDeedWrapper;
import jason.architecture.api.app.model.state.plan.PlanWrapper;
import jason.architecture.api.app.model.state.term.StructureWrapper;
import jason.architecture.api.app.model.state.term.TermWrapper;
import jason.architecture.api.app.model.state.term.TriggerWrapper;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PlanWrapperHandler {

    private static final String KQML_PREFIX = "@kqml";

    private final Agent agent;

    public PlanWrapper extractPlan(Plan plan, Unifier unifier, PlanBody executedDeed) {
        TermWrapperHandler termWrapperHandler = new TermWrapperHandler();

        TriggerWrapper trigger = termWrapperHandler.extractTrigger(plan.getTrigger(), unifier);

        Pred label = plan.getLabel();
        List<TermWrapper> annotations = new ArrayList<>();
        if (label != null && label.getAnnots() != null) {
            for (Term annotation : label.getAnnots()) {
                if (annotation.toString().contains("url")) {
                    continue;
                }
                termWrapperHandler.extractAll(annotations, annotation, unifier);
            }
        }

        List<TermWrapper> context = null;
        if (plan.getContext() != null) {
            context = new ArrayList<>();
            termWrapperHandler.extractAll(context, plan.getContext(), unifier);
        }

        List<PlanDeedWrapper> body = new ArrayList<>();
        PlanBody pb = plan.getBody();
        while (pb != null) {
            PlanDeedWrapper deed = this.extractPlanBody(pb, unifier);
            if (pb.equals(executedDeed)) {
                deed.setSelected(true);
            }
            body.add(deed);
            pb = pb.getBodyNext();
        }

        return new PlanWrapper(trigger, plan.getFunctor(), plan.getNS().toString(), context, body, annotations,
                termWrapperHandler.extractSourceInfo(plan));
    }

    public PlanDeedWrapper extractPlanBody(PlanBody planBody, Unifier unifier) {
        TermWrapperHandler termWrapperHandler = new TermWrapperHandler();

        Structure bodyTerm = (Structure) planBody.getBodyTerm();
        PlanBodyImpl planBodyImpl = (PlanBodyImpl) planBody;

        StructureWrapper deedTerm = termWrapperHandler.extractStructure(bodyTerm, unifier);

        return new PlanDeedWrapper(deedTerm, planBodyImpl.getFunctor(), planBodyImpl.getBodyType().toString(),
                deedTerm.getNamespace(), termWrapperHandler.extractSourceInfo(planBody));
    }

    public List<PlanWrapper> extractAll() {
        List<PlanWrapper> planWrappers = new ArrayList<>();

        List<Plan> allPlans = agent.getPL().getPlans();
        for (Plan plan : allPlans) {
            if (plan.toASString().startsWith(KQML_PREFIX)) {
                continue;
            }

            PlanWrapper planWrapper = this.extractPlan(plan, null, null);
            planWrappers.add(planWrapper);
        }

        return planWrappers;
    }

}
