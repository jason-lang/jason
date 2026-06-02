package jason.mind.api.infrastructure.mapper.intention;

import jason.asSemantics.Agent;
import jason.mind.api.application.snapshot.model.intention.IntentionSnapshot;
import jason.mind.api.application.snapshot.model.plan.PlanSnapshot;

import java.util.List;

public interface IntentionSnapshotMapper {
    List<PlanSnapshot> extractRunningPlans(Agent agent);

    List<IntentionSnapshot> extractAll(Agent agent);
}
