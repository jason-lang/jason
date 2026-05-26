package jason.mind.api.infrastructure.mapper.belief;

import jason.asSemantics.Agent;
import jason.mind.api.application.snapshot.model.BeliefSnapshot;
import jason.mind.api.application.snapshot.model.term.PredWrapper;
import jason.mind.api.application.snapshot.port.out.AgentSnapshotRepository;

import java.util.List;

public interface BeliefSnapshotMapper {
    List<PredWrapper> extractRules(Agent agent);

    List<BeliefSnapshot> extractBaseBeliefs(Agent agent, AgentSnapshotRepository snapshotRepository);
}
