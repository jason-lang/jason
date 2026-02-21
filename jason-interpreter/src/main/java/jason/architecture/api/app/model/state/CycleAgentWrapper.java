package jason.architecture.api.app.model.state;

import lombok.Getter;

@Getter
public class CycleAgentWrapper extends AgentWrapper {

    private final CycleInfo cycleInfo;

    public CycleAgentWrapper(AgentWrapper agentWrapper, CycleInfo cycleInfo) {
        super(agentWrapper.getName(), agentWrapper.getIntentions(), agentWrapper.getRunningPlans(),
                agentWrapper.getBeliefs(), agentWrapper.getRules(), agentWrapper.getAllPlans(),
                agentWrapper.getMessageBox(), agentWrapper.getTime());
        this.cycleInfo = cycleInfo;
    }
}
