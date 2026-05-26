package jason.mind.api.infrastructure.mapper.belief;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Rule;
import jason.mind.api.application.snapshot.model.BeliefSnapshot;
import jason.mind.api.application.snapshot.model.CycleAgentSnapshot;
import jason.mind.api.application.snapshot.model.term.PredWrapper;
import jason.mind.api.application.snapshot.model.term.TermWrapper;
import jason.mind.api.application.snapshot.port.out.AgentSnapshotRepository;
import jason.mind.api.infrastructure.mapper.term.TermMapper;

import java.util.ArrayList;
import java.util.List;

public final class StandardBeliefSnapshotMapper implements BeliefSnapshotMapper {

    private static final String KQML_BELIEF_SOURCE_FILE_STRING = "kqml";
    private final TermMapper termMapper;

    public StandardBeliefSnapshotMapper(TermMapper termMapper) {
        this.termMapper = termMapper;
    }

    @Override
    public List<PredWrapper> extractRules(Agent agent) {
        List<PredWrapper> rules = new ArrayList<>();

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

    @Override
    public List<BeliefSnapshot> extractBaseBeliefs(Agent agent, AgentSnapshotRepository snapshotRepository) {
        List<BeliefSnapshot> beliefs = new ArrayList<>();
        int currentCycle = agent.getTS().getAgArch().getCycleNumber();
        int previousCycle = currentCycle - 1;
        String agentName = agent.getTS().getAgArch().getAgName();
        CycleAgentSnapshot previousState = snapshotRepository.findSnapshot(agentName, previousCycle);

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
