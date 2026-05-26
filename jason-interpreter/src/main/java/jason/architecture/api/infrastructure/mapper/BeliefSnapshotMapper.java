package jason.architecture.api.infrastructure.mapper;

import jason.architecture.api.application.snapshot.model.BeliefSnapshot;
import jason.architecture.api.application.snapshot.model.CycleAgentSnapshot;
import jason.architecture.api.application.snapshot.model.term.PredWrapper;
import jason.architecture.api.application.snapshot.model.term.TermWrapper;
import jason.architecture.api.application.snapshot.port.out.AgentSnapshotRepository;
import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Rule;

import java.util.ArrayList;
import java.util.List;

public class BeliefSnapshotMapper {

    private static final String KQML_BELIEF_SOURCE_FILE_STRING = "kqml";

    private final Agent agent;

    private final AgentSnapshotRepository snapshotRepository;

    public BeliefSnapshotMapper(Agent agent, AgentSnapshotRepository snapshotRepository) {
        this.agent = agent;
        this.snapshotRepository = snapshotRepository;
    }

    public List<PredWrapper> extractRules() {
        List<PredWrapper> rules = new ArrayList<>();

        TermMapper termMapper = new TermMapper();

        for (Literal ruleTerm : agent.getTS().getAg().getBB()) {
            if (ruleTerm.getSrcInfo().getSrcFile().contains(KQML_BELIEF_SOURCE_FILE_STRING)
                    || !(ruleTerm instanceof Rule)) {
                continue;
            }

            PredWrapper rule = termMapper.extractPred((Pred) ruleTerm, null);
            List<TermWrapper> body = new ArrayList<>();
            termMapper.extractAll(body, ((Rule) ruleTerm).getBody(), null);
            rule.setTerms(body);
            rules.add(rule);
        }

        return rules;
    }

    public List<BeliefSnapshot> extractBaseBeliefs() {
        List<BeliefSnapshot> beliefs = new ArrayList<>();

        int currentCycle = this.agent.getTS().getAgArch().getCycleNumber();
        int previousCycle = currentCycle - 1;

        String agentName = this.agent.getTS().getAgArch().getAgName();
        CycleAgentSnapshot previousState = this.snapshotRepository.findSnapshot(agentName, previousCycle);

        TermMapper termMapper = new TermMapper();

        for (Literal beliefTerm : agent.getTS().getAg().getBB()) {
            if (beliefTerm.getSrcInfo().getSrcFile().contains(KQML_BELIEF_SOURCE_FILE_STRING)
                    || beliefTerm instanceof Rule || !(beliefTerm instanceof LiteralImpl)) {
                continue;
            }

            PredWrapper beliefPred = termMapper.extractPred((Pred) beliefTerm, null);
            BeliefSnapshot belief = new BeliefSnapshot(beliefPred);

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
