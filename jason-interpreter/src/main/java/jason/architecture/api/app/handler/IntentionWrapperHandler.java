package jason.architecture.api.app.handler;

import jason.architecture.api.app.model.state.intention.GoalWrapper;
import jason.architecture.api.app.model.state.intention.IntentionStep;
import jason.architecture.api.app.model.state.intention.IntentionWrapper;
import jason.architecture.api.app.model.state.plan.PlanWrapper;
import jason.architecture.api.app.model.state.term.TriggerWrapper;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSyntax.PlanBody;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IntentionWrapperHandler {

    private static final Map<String, Map<Integer, List<IntendedMeans>>> LAST_INTENDED_MEANS_BY_AGENT_AND_CYCLE
            = new ConcurrentHashMap<>();

    public List<PlanWrapper> extractRunningPlans(Agent agent) {
        TermWrapperHandler termWrapperHandler = new TermWrapperHandler();
        PlanWrapperHandler planWrapperHandler = new PlanWrapperHandler(agent);

        List<PlanWrapper> plans = new ArrayList<>();

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
                TriggerWrapper goalTrigger = termWrapperHandler.extractTrigger(im.getTrigger(), im.getUnif());

                if (goalTrigger == null) {
                    continue;
                }

                GoalWrapper goalWrapper = new GoalWrapper(goalTrigger, null);

                PlanWrapper plan = planWrapperHandler.extractPlan(im.getPlan(), im.getUnif(), lastDeed);

                plan.setGoalTrigger(goalWrapper.getTrigger());
                plans.add(plan);
            }
        });

        return plans;
    }

    public List<IntentionWrapper> extractAll(Agent agent) {
        TermWrapperHandler termWrapperHandler = new TermWrapperHandler();

        List<IntentionWrapper> intentions = new ArrayList<>();

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

            List<GoalWrapper> stackGoals = new ArrayList<>();

            for (IntendedMeans im : intendedMeans) {
                TriggerWrapper goalTrigger = termWrapperHandler.extractTrigger(im.getTrigger(), im.getUnif());

                if (goalTrigger == null) {
                    return;
                }

                GoalWrapper goalWrapper = new GoalWrapper(goalTrigger, null);
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

            IntentionWrapper intentionWrapper = new IntentionWrapper(intention.getId(), intentionStep.toString());
            intentionWrapper.getStackGoals().addAll(stackGoals);
            intentions.add(intentionWrapper);
        });

        return intentions;
    }
}
