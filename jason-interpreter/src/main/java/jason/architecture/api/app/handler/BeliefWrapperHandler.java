package jason.architecture.api.app.handler;

import jason.architecture.api.app.model.state.BeliefWrapper;
import jason.architecture.api.app.model.state.CycleAgentWrapper;
import jason.architecture.api.app.model.state.term.PredWrapper;
import jason.architecture.api.app.model.state.term.TermWrapper;
import jason.architecture.api.app.state.AgentStateManager;
import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Rule;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BeliefWrapperHandler {

    private static final String KQML_BELIEF_SOURCE_FILE_STRING = "kqml";

    private final Agent agent;

    public List<PredWrapper> extractRules() {
        List<PredWrapper> rules = new ArrayList<>();

        TermWrapperHandler termWrapperHandler = new TermWrapperHandler();

        for (Literal ruleTerm : agent.getTS().getAg().getBB()) {
            if (ruleTerm.getSrcInfo().getSrcFile().contains(KQML_BELIEF_SOURCE_FILE_STRING)
                    || !(ruleTerm instanceof Rule)) {
                continue;
            }

            PredWrapper rule = termWrapperHandler.extractPred((Pred) ruleTerm, null);
            List<TermWrapper> body = new ArrayList<>();
            termWrapperHandler.extractAll(body, ((Rule) ruleTerm).getBody(), null);
            rule.setTerms(body);
            rules.add(rule);
        }

        return rules;
    }

    public List<BeliefWrapper> extractBaseBeliefs() {
        List<BeliefWrapper> beliefs = new ArrayList<>();

        int currentCycle = this.agent.getTS().getAgArch().getCycleNumber();
        int previousCycle = currentCycle - 1;

        String agentName = this.agent.getTS().getAgArch().getAgName();
        CycleAgentWrapper previousState = AgentStateManager.getInstance().getState(agentName, previousCycle);

        TermWrapperHandler termWrapperHandler = new TermWrapperHandler();

        for (Literal beliefTerm : agent.getTS().getAg().getBB()) {
            if (beliefTerm.getSrcInfo().getSrcFile().contains(KQML_BELIEF_SOURCE_FILE_STRING)
                    || beliefTerm instanceof Rule || !(beliefTerm instanceof LiteralImpl)) {
                continue;
            }

            PredWrapper beliefPred = termWrapperHandler.extractPred((Pred) beliefTerm, null);
            BeliefWrapper belief = new BeliefWrapper(beliefPred);

            if (previousState != null) {
                int index = previousState.getBeliefs().indexOf(belief);
                if (index == -1) {
                    belief.setCycle(currentCycle);
                } else {
                    belief = previousState.getBeliefs().get(index);
                }
            } else {
                belief.setCycle(1);
            }

            beliefs.add(belief);
        }

        return beliefs;
    }

}
