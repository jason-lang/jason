package jason.architecture.api.infrastructure.mapper;

import jason.architecture.api.application.snapshot.model.plan.PlanDeedSnapshot;
import jason.architecture.api.application.snapshot.model.plan.PlanSnapshot;
import jason.architecture.api.application.snapshot.model.term.StructureWrapper;
import jason.architecture.api.application.snapshot.model.term.TermWrapper;
import jason.architecture.api.application.snapshot.model.term.TriggerWrapper;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PlanSnapshotMapper {

    private static final String KQML_PREFIX = "@kqml";

    private final Agent agent;

    public PlanSnapshot extractPlan(Plan plan, Unifier unifier, PlanBody executedDeed) {
        TermMapper termMapper = new TermMapper();

        TriggerWrapper trigger = termMapper.extractTrigger(plan.getTrigger(), unifier);

        Pred label = plan.getLabel();
        List<TermWrapper> annotations = new ArrayList<>();
        if (label != null && label.getAnnots() != null) {
            for (Term annotation : label.getAnnots()) {
                if (annotation.toString().contains("url")) {
                    continue;
                }
                termMapper.extractAll(annotations, annotation, unifier);
            }
        }

        List<TermWrapper> context = null;
        if (plan.getContext() != null) {
            context = new ArrayList<>();
            termMapper.extractAll(context, plan.getContext(), unifier);
        }

        List<PlanDeedSnapshot> body = new ArrayList<>();
        PlanBody pb = plan.getBody();
        while (pb != null) {
            PlanDeedSnapshot deed = this.extractPlanBody(pb, unifier);
            if (pb.equals(executedDeed)) {
                deed.setSelected(true);
            }
            body.add(deed);
            pb = pb.getBodyNext();
        }

        return new PlanSnapshot(trigger, plan.getFunctor(), plan.getNS().toString(), context, body, annotations,
                termMapper.extractSourceInfo(plan));
    }

    public PlanDeedSnapshot extractPlanBody(PlanBody planBody, Unifier unifier) {
        TermMapper termMapper = new TermMapper();

        Structure bodyTerm = (Structure) planBody.getBodyTerm();
        PlanBodyImpl planBodyImpl = (PlanBodyImpl) planBody;

        StructureWrapper deedTerm = termMapper.extractStructure(bodyTerm, unifier);

        return new PlanDeedSnapshot(deedTerm, planBodyImpl.getFunctor(), planBodyImpl.getBodyType().toString(),
                deedTerm.getNamespace(), termMapper.extractSourceInfo(planBody));
    }

    public List<PlanSnapshot> extractAll() {
        List<PlanSnapshot> planWrappers = new ArrayList<>();

        List<Plan> allPlans = agent.getPL().getPlans();
        for (Plan plan : allPlans) {
            if (plan.toASString().startsWith(KQML_PREFIX)) {
                continue;
            }

            PlanSnapshot planWrapper = this.extractPlan(plan, null, null);
            planWrappers.add(planWrapper);
        }

        return planWrappers;
    }

}
