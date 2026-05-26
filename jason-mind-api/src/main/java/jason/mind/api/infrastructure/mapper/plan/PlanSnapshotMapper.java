package jason.mind.api.infrastructure.mapper.plan;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.mind.api.application.snapshot.model.plan.PlanDeedSnapshot;
import jason.mind.api.application.snapshot.model.plan.PlanSnapshot;

import java.util.List;

public interface PlanSnapshotMapper {
    PlanSnapshot extractPlan(Agent agent, Plan plan, Unifier unifier, PlanBody executedDeed);

    PlanDeedSnapshot extractPlanBody(PlanBody planBody, Unifier unifier);

    List<PlanSnapshot> extractAll(Agent agent);
}
