package jason.mind.api.infrastructure.mapper.intention;

import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSyntax.PlanBody;
import jason.mind.api.application.snapshot.model.intention.GoalSnapshot;
import jason.mind.api.application.snapshot.model.intention.IntentionSnapshot;
import jason.mind.api.application.snapshot.model.intention.IntentionStep;
import jason.mind.api.application.snapshot.model.plan.PlanSnapshot;
import jason.mind.api.application.snapshot.model.term.TriggerWrapper;
import jason.mind.api.infrastructure.mapper.plan.PlanSnapshotMapper;
import jason.mind.api.infrastructure.mapper.term.TermMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class StandardIntentionSnapshotMapper implements IntentionSnapshotMapper {

    private final TermMapper termMapper;
    private final PlanSnapshotMapper planMapper;
    private final IntentionSnapshotMemory memory = IntentionSnapshotMemory.getInstance();

    public StandardIntentionSnapshotMapper(TermMapper termMapper, PlanSnapshotMapper planMapper) {
        this.termMapper = termMapper;
        this.planMapper = planMapper;
    }

    @Override
    public List<PlanSnapshot> extractRunningPlans(Agent agent) {
        List<PlanSnapshot> plans = new ArrayList<>();
        Circumstance circumstance = agent.getTS().getC();
        String agentName = agent.getTS().getAgArch().getAgName();

        circumstance.getAllIntentions().forEachRemaining((intention) -> {
            Map<Integer, PlanBody> lastDeeds = circumstance.getLastDeed();
            PlanBody lastDeed = lastDeeds.get(intention.getId());

            List<IntendedMeans> intendedMeans = snapshotIntendedMeans(agentName, intention);
            for (IntendedMeans im : intendedMeans) {
                TriggerWrapper goalTrigger = termMapper.extractTrigger(im.getTrigger(), im.getUnif());
                if (goalTrigger == null) {
                    continue;
                }

                GoalSnapshot goalWrapper = new GoalSnapshot(goalTrigger, null);
                PlanSnapshot plan = planMapper.extractPlan(agent, im.getPlan(), im.getUnif(), lastDeed);
                plan.setGoalTrigger(goalWrapper.getTrigger());
                plans.add(plan);
            }
        });

        return plans;
    }

    @Override
    public List<IntentionSnapshot> extractAll(Agent agent) {
        List<IntentionSnapshot> intentions = new ArrayList<>();
        Circumstance circumstance = agent.getTS().getC();
        String agentName = agent.getTS().getAgArch().getAgName();

        Intention selectedIntention = circumstance.getSelectedIntention();
        Collection<Intention> pendingIntentions = circumstance.getPendingIntentions().values();

        circumstance.getAllIntentions().forEachRemaining((intention) -> {
            List<IntendedMeans> intendedMeans = snapshotIntendedMeans(agentName, intention);
            List<GoalSnapshot> stackGoals = new ArrayList<>();

            for (IntendedMeans im : intendedMeans) {
                TriggerWrapper goalTrigger = termMapper.extractTrigger(im.getTrigger(), im.getUnif());
                if (goalTrigger == null) {
                    continue;
                }
                stackGoals.add(new GoalSnapshot(goalTrigger, null));
            }

            IntentionStep intentionStep;
            if (intention.equals(selectedIntention)) {
                intentionStep = IntentionStep.selected;
            } else if (pendingIntentions.contains(intention)) {
                intentionStep = IntentionStep.pending;
            } else {
                intentionStep = IntentionStep.queue;
            }

            IntentionSnapshot intentionWrapper = new IntentionSnapshot(intention.getId(), intentionStep.toString());
            intentionWrapper.getStackGoals().addAll(stackGoals);
            intentions.add(intentionWrapper);
        });

        return intentions;
    }

    private List<IntendedMeans> snapshotIntendedMeans(String agentName, Intention intention) {
        if (!intention.isFinished()) {
            List<IntendedMeans> intendedMeans = new ArrayList<>();
            intention.iterator().forEachRemaining(im -> intendedMeans.add((IntendedMeans) im.clone()));
            memory.remember(agentName, intention.getId(), intendedMeans);
            return intendedMeans;
        }

        return memory.recall(agentName, intention.getId());
    }
}
