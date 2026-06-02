package jason.mind.api.application.snapshot.model;

import lombok.Getter;

@Getter
public class CycleAgentSnapshot extends AgentSnapshot {

    private final CycleInfo cycleInfo;

    public CycleAgentSnapshot(AgentSnapshot agentSnapshot, CycleInfo cycleInfo) {
        super(agentSnapshot.getName(), agentSnapshot.getIntentions(), agentSnapshot.getRunningPlans(),
                agentSnapshot.getBeliefs(), agentSnapshot.getRules(), agentSnapshot.getAllPlans(),
                agentSnapshot.getMessageBox(), agentSnapshot.getTime());
        this.cycleInfo = cycleInfo;
    }
}
