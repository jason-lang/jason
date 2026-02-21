package jason.architecture.api.jason;

import jason.architecture.api.app.handler.BeliefWrapperHandler;
import jason.architecture.api.app.handler.IntentionWrapperHandler;
import jason.architecture.api.app.handler.MessageWrapperHandler;
import jason.architecture.api.app.handler.PlanWrapperHandler;
import jason.architecture.api.app.model.state.AgentWrapper;
import jason.architecture.api.app.model.state.BeliefWrapper;
import jason.architecture.api.app.model.state.MessageWrapper;
import jason.architecture.api.app.model.state.intention.IntentionWrapper;
import jason.architecture.api.app.model.state.plan.PlanWrapper;
import jason.architecture.api.app.model.state.term.PredWrapper;
import jason.architecture.api.app.state.AgentStateManager;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;

import java.util.Date;
import java.util.List;

/**
 * Arquitetura do Mind API.
 */
public class MindApiArch extends AgArch {

    private List<MessageWrapper> messages;

    @Override
    public void reasoningCycleStarting() {
        Agent agent = this.getTS().getAg();
        Date now = new Date(System.currentTimeMillis());

        MessageWrapperHandler messageWrapperHandler = new MessageWrapperHandler(agent, now);
        this.messages = messageWrapperHandler.extractAll();
        super.reasoningCycleStarting();
    }

    @Override
    public void reasoningCycleFinished() {
        Agent agent = this.getTS().getAg();

        Date now = new Date(System.currentTimeMillis());

        BeliefWrapperHandler beliefWrapperHandler = new BeliefWrapperHandler(agent);
        List<BeliefWrapper> beliefs = beliefWrapperHandler.extractBaseBeliefs();
        List<PredWrapper> rules = beliefWrapperHandler.extractRules();

        IntentionWrapperHandler intentionWrapperHandler = new IntentionWrapperHandler();
        List<IntentionWrapper> intentionWrappers = intentionWrapperHandler.extractAll(agent);
        List<PlanWrapper> runningPlans = intentionWrapperHandler.extractRunningPlans(agent);
        List<PlanWrapper> allPlans = new PlanWrapperHandler(agent).extractAll();

        AgentWrapper agentWrapper = new AgentWrapper(agent.getTS().getUserAgArch().getAgName(), intentionWrappers,
                runningPlans, beliefs, rules, allPlans, this.messages, now);

        AgentStateManager.getInstance().addState(agentWrapper, super.getCycleNumber());

        super.reasoningCycleFinished();
    }
}
