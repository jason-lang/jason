package jason.architecture.api.infrastructure.mapper;

import jason.architecture.api.application.snapshot.model.intention.GoalSnapshot;
import jason.architecture.api.application.snapshot.model.intention.IntentionStep;
import jason.architecture.api.application.snapshot.model.intention.IntentionSnapshot;
import jason.architecture.api.application.snapshot.model.plan.PlanSnapshot;
import jason.architecture.api.application.snapshot.model.term.TriggerWrapper;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSyntax.PlanBody;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IntentionSnapshotMapper {

    private static final Map<String, Map<Integer, List<IntendedMeans>>> LAST_INTENDED_MEANS_BY_AGENT_AND_CYCLE
            = new ConcurrentHashMap<>();

    public List<PlanSnapshot> extractRunningPlans(Agent agent) {
        TermMapper termMapper = new TermMapper();
        PlanSnapshotMapper planMapper = new PlanSnapshotMapper(agent);

        List<PlanSnapshot> plans = new ArrayList<>();

        Circumstance circumstance = agent.getTS().getC();

        Map<Integer, List<IntendedMeans>> lastIntendedMeansByCycle
                = LAST_INTENDED_MEANS_BY_AGENT_AND_CYCLE.computeIfAbsent(agent.getTS().getAgArch().getAgName(),
                k -> new HashMap<>());

        circumstance.getAllIntentions().forEachRemaining((intention) -> {
            Map<Integer, PlanBody> lastDeeds = circumstance.getLastDeed();
            PlanBody lastDeed = lastDeeds.get(intention.getId());

            List<IntendedMeans> intendedMeans = new ArrayList<>();
            if (!intention.isFinished()) {
                intention.iterator().forEachRemaining(im -> intendedMeans.add((IntendedMeans) im.clone()));
                lastIntendedMeansByCycle.computeIfAbsent(intention.getId(), k -> intendedMeans);
            } else {
                List<IntendedMeans> intendedMeans1 = lastIntendedMeansByCycle.get(intention.getId());
                intendedMeans.addAll(intendedMeans1);
            }

            for (IntendedMeans im : intendedMeans) {
                TriggerWrapper goalTrigger = termMapper.extractTrigger(im.getTrigger(), im.getUnif());

                if (goalTrigger == null) {
                    continue;
                }

                GoalSnapshot goalWrapper = new GoalSnapshot(goalTrigger, null);

                PlanSnapshot plan = planMapper.extractPlan(im.getPlan(), im.getUnif(), lastDeed);

                plan.setGoalTrigger(goalWrapper.getTrigger());
                plans.add(plan);
            }
        });

        return plans;
    }

    public List<IntentionSnapshot> extractAll(Agent agent) {
        TermMapper termMapper = new TermMapper();

        List<IntentionSnapshot> intentions = new ArrayList<>();

        Circumstance circumstance = agent.getTS().getC();

        Intention selectedIntention = circumstance.getSelectedIntention();
        Collection<Intention> pendingIntentions = circumstance.getPendingIntentions().values();

        Map<Integer, List<IntendedMeans>> lastIntendedMeansByCycle
                = LAST_INTENDED_MEANS_BY_AGENT_AND_CYCLE.computeIfAbsent(agent.getTS().getAgArch().getAgName(),
                k -> new HashMap<>());

        circumstance.getAllIntentions().forEachRemaining((intention) -> {
            List<IntendedMeans> intendedMeans = new ArrayList<>();
            if (!intention.isFinished()) {
                intention.iterator().forEachRemaining(im -> intendedMeans.add((IntendedMeans) im.clone()));
                lastIntendedMeansByCycle.computeIfAbsent(intention.getId(), k -> intendedMeans);
            } else {
                List<IntendedMeans> intendedMeans1 = lastIntendedMeansByCycle.get(intention.getId());
                intendedMeans.addAll(intendedMeans1);
            }

            List<GoalSnapshot> stackGoals = new ArrayList<>();

            for (IntendedMeans im : intendedMeans) {
                TriggerWrapper goalTrigger = termMapper.extractTrigger(im.getTrigger(), im.getUnif());

                if (goalTrigger == null) {
                    return;
                }

                GoalSnapshot goalWrapper = new GoalSnapshot(goalTrigger, null);
                stackGoals.add(goalWrapper);
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
}
