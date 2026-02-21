package jason.architecture.api.app.handler;

import jason.architecture.api.app.model.state.AgentWrapper;
import jason.architecture.api.app.model.state.BeliefWrapper;
import jason.architecture.api.app.model.state.MessageWrapper;
import jason.architecture.api.app.model.state.intention.IntentionWrapper;
import jason.architecture.api.app.model.state.plan.PlanWrapper;
import jason.architecture.api.app.model.state.term.PredWrapper;
import jason.asSemantics.Agent;

import java.util.Date;
import java.util.List;

public class AgentWrapperHandler {

    public AgentWrapper extract(Agent agent) {
        Date now = new Date(System.currentTimeMillis());

        BeliefWrapperHandler beliefWrapperHandler = new BeliefWrapperHandler(agent);
        List<BeliefWrapper> beliefs = beliefWrapperHandler.extractBaseBeliefs();
        List<PredWrapper> rules = beliefWrapperHandler.extractRules();

        IntentionWrapperHandler intentionWrapperHandler = new IntentionWrapperHandler();
        List<IntentionWrapper> intentions = intentionWrapperHandler.extractAll(agent);
        List<PlanWrapper> instantiatedPlansByIntentions = intentionWrapperHandler.extractRunningPlans(agent);
        List<PlanWrapper> allPlans = new PlanWrapperHandler(agent).extractAll();

        MessageWrapperHandler messageWrapperHandler = new MessageWrapperHandler(agent, now);
        List<MessageWrapper> messageWrappers = messageWrapperHandler.extractAll();

        return new AgentWrapper(agent.getTS().getUserAgArch().getAgName(), intentions, instantiatedPlansByIntentions,
                beliefs, rules, allPlans, messageWrappers, now);
    }

}
