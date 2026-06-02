package jason.mind.api.infrastructure.mapper.plan;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.mind.api.application.snapshot.model.plan.PlanDeedSnapshot;
import jason.mind.api.application.snapshot.model.plan.PlanSnapshot;
import jason.mind.api.application.snapshot.model.term.StructureWrapper;
import jason.mind.api.application.snapshot.model.term.TermWrapper;
import jason.mind.api.application.snapshot.model.term.TriggerWrapper;
import jason.mind.api.infrastructure.mapper.term.TermMapper;

import java.util.ArrayList;
import java.util.List;

public final class StandardPlanSnapshotMapper implements PlanSnapshotMapper {

    private static final String KQML_PREFIX = "@kqml";
    private final TermMapper termMapper;

    public StandardPlanSnapshotMapper(TermMapper termMapper) {
        this.termMapper = termMapper;
    }

    @Override
    public PlanSnapshot extractPlan(Agent agent, Plan plan, Unifier unifier, PlanBody executedDeed) {
        TriggerWrapper trigger = termMapper.extractTrigger(plan.getTrigger(), unifier);

        Pred label = plan.getLabel();
        List<TermWrapper> annotations = new ArrayList<>();
        if (label != null && label.getAnnots() != null) {
            for (Term annotation : label.getAnnots()) {
                if (!annotation.toString().contains("url")) {
                    termMapper.extractAll(annotations, annotation, unifier);
                }
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
            if (pb.getBodyTerm() == null) {
                break;
            }
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

    @Override
    public PlanDeedSnapshot extractPlanBody(PlanBody planBody, Unifier unifier) {
        Structure bodyTerm = (Structure) planBody.getBodyTerm();
        PlanBodyImpl planBodyImpl = (PlanBodyImpl) planBody;
        StructureWrapper deedTerm = termMapper.extractStructure(bodyTerm, unifier);

        return new PlanDeedSnapshot(deedTerm, planBodyImpl.getFunctor(), planBodyImpl.getBodyType().toString(),
                deedTerm.getNamespace(), termMapper.extractSourceInfo(planBody));
    }

    @Override
    public List<PlanSnapshot> extractAll(Agent agent) {
        List<PlanSnapshot> planWrappers = new ArrayList<>();
        for (Plan plan : agent.getPL().getPlans()) {
            if (!plan.toASString().startsWith(KQML_PREFIX)) {
                planWrappers.add(this.extractPlan(agent, plan, null, null));
            }
        }
        return planWrappers;
    }
}
