package jason.architecture.api.infrastructure.adapter.in.jason;

import jason.architecture.AgArch;
import jason.architecture.api.application.snapshot.model.term.PredWrapper;
import jason.architecture.api.application.snapshot.model.AgentSnapshot;
import jason.architecture.api.application.snapshot.model.BeliefSnapshot;
import jason.architecture.api.application.snapshot.model.MessageSnapshot;
import jason.architecture.api.application.snapshot.model.intention.IntentionSnapshot;
import jason.architecture.api.application.snapshot.model.plan.PlanSnapshot;
import jason.architecture.api.application.snapshot.port.out.AgentSnapshotRepository;
import jason.architecture.api.infrastructure.mapper.BeliefSnapshotMapper;
import jason.architecture.api.infrastructure.mapper.IntentionSnapshotMapper;
import jason.architecture.api.infrastructure.mapper.MessageSnapshotMapper;
import jason.architecture.api.infrastructure.mapper.PlanSnapshotMapper;
import jason.architecture.api.infrastructure.adapter.out.memory.InMemoryMindRepository;
import jason.asSemantics.Agent;

import java.util.Date;
import java.util.List;

/**
 * Arquitetura do Mind API.
 */
public class MindApiArch extends AgArch {

    private final AgentSnapshotRepository snapshotRepository = InMemoryMindRepository.getInstance();

    private List<MessageSnapshot> messages;

    @Override
    public void reasoningCycleStarting() {
        Agent agent = this.getTS().getAg();
        Date now = new Date(System.currentTimeMillis());

        MessageSnapshotMapper messageMapper = new MessageSnapshotMapper(agent, now);
        this.messages = messageMapper.extractAll();
        super.reasoningCycleStarting();
    }

    @Override
    public void reasoningCycleFinished() {
        Agent agent = this.getTS().getAg();

        Date now = new Date(System.currentTimeMillis());

        BeliefSnapshotMapper beliefMapper = new BeliefSnapshotMapper(agent, this.snapshotRepository);
        List<BeliefSnapshot> beliefs = beliefMapper.extractBaseBeliefs();
        List<PredWrapper> rules = beliefMapper.extractRules();

        IntentionSnapshotMapper intentionMapper = new IntentionSnapshotMapper();
        List<IntentionSnapshot> intentionSnapshots = intentionMapper.extractAll(agent);
        List<PlanSnapshot> runningPlans = intentionMapper.extractRunningPlans(agent);
        List<PlanSnapshot> allPlans = new PlanSnapshotMapper(agent).extractAll();

        AgentSnapshot agentSnapshot = new AgentSnapshot(agent.getTS().getUserAgArch().getAgName(), intentionSnapshots,
                runningPlans, beliefs, rules, allPlans, this.messages, now);

        this.snapshotRepository.saveSnapshot(agentSnapshot, super.getCycleNumber());

        super.reasoningCycleFinished();
    }
}
