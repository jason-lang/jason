package jason.mind.api.infrastructure.adapter.in.jason;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mind.api.application.snapshot.model.AgentSnapshot;
import jason.mind.api.application.snapshot.model.BeliefSnapshot;
import jason.mind.api.application.snapshot.model.MessageSnapshot;
import jason.mind.api.application.snapshot.model.intention.IntentionSnapshot;
import jason.mind.api.application.snapshot.model.plan.PlanSnapshot;
import jason.mind.api.application.snapshot.model.term.PredWrapper;
import jason.mind.api.application.snapshot.port.out.AgentSnapshotRepository;
import jason.mind.api.infrastructure.adapter.out.memory.InMemoryMindRepository;
import jason.mind.api.infrastructure.mapper.belief.BeliefSnapshotMapper;
import jason.mind.api.infrastructure.mapper.intention.IntentionSnapshotMapper;
import jason.mind.api.infrastructure.mapper.message.MessageSnapshotMapper;
import jason.mind.api.infrastructure.mapper.plan.PlanSnapshotMapper;

import java.util.Date;

import jason.mind.api.infrastructure.mapper.Mappers;
import java.util.List;

public class MindApiArch extends AgArch {

    private final AgentSnapshotRepository snapshotRepository = InMemoryMindRepository.getInstance();

    private final MessageSnapshotMapper messageSnapshotMapper = Mappers.getInstance().message();

    private final BeliefSnapshotMapper beliefSnapshotMapper = Mappers.getInstance().belief();

    private final IntentionSnapshotMapper intentionSnapshotMapper = Mappers.getInstance().intention();

    private final PlanSnapshotMapper planSnapshotMapper = Mappers.getInstance().plan();

    private List<MessageSnapshot> messages;

    @Override
    public void reasoningCycleStarting() {
        Agent agent = this.getTS().getAg();
        Date now = new Date(System.currentTimeMillis());

        this.messages = messageSnapshotMapper.extractAll(agent, now);
        super.reasoningCycleStarting();
    }

    @Override
    public void reasoningCycleFinished() {
        Agent agent = this.getTS().getAg();
        Date now = new Date(System.currentTimeMillis());

        List<BeliefSnapshot> beliefs = beliefSnapshotMapper.extractBaseBeliefs(agent, this.snapshotRepository);
        List<PredWrapper> rules = beliefSnapshotMapper.extractRules(agent);
        List<IntentionSnapshot> intentionSnapshots = intentionSnapshotMapper.extractAll(agent);
        List<PlanSnapshot> runningPlans = intentionSnapshotMapper.extractRunningPlans(agent);
        List<PlanSnapshot> allPlans = planSnapshotMapper.extractAll(agent);

        AgentSnapshot agentSnapshot = new AgentSnapshot(agent.getTS().getUserAgArch().getAgName(), intentionSnapshots,
                runningPlans, beliefs, rules, allPlans, this.messages, now);

        this.snapshotRepository.saveSnapshot(agentSnapshot, super.getCycleNumber());
        super.reasoningCycleFinished();
    }
}
