package jason.mind.api.infrastructure.mapper;

import jason.mind.api.infrastructure.mapper.belief.BeliefSnapshotMapper;
import jason.mind.api.infrastructure.mapper.belief.StandardBeliefSnapshotMapper;
import jason.mind.api.infrastructure.mapper.intention.IntentionSnapshotMapper;
import jason.mind.api.infrastructure.mapper.intention.StandardIntentionSnapshotMapper;
import jason.mind.api.infrastructure.mapper.message.MessageSnapshotMapper;
import jason.mind.api.infrastructure.mapper.message.StandardMessageSnapshotMapper;
import jason.mind.api.infrastructure.mapper.plan.PlanSnapshotMapper;
import jason.mind.api.infrastructure.mapper.plan.StandardPlanSnapshotMapper;
import jason.mind.api.infrastructure.mapper.term.StandardTermMapper;
import jason.mind.api.infrastructure.mapper.term.TermMapper;

public final class Mappers {

    private static final Mappers INSTANCE = new Mappers();

    private final TermMapper termMapper = new StandardTermMapper();

    private final BeliefSnapshotMapper beliefSnapshotMapper = new StandardBeliefSnapshotMapper(termMapper);

    private final PlanSnapshotMapper planSnapshotMapper = new StandardPlanSnapshotMapper(termMapper);

    private final IntentionSnapshotMapper intentionSnapshotMapper = new StandardIntentionSnapshotMapper(termMapper,
            planSnapshotMapper);

    private final MessageSnapshotMapper messageSnapshotMapper = new StandardMessageSnapshotMapper(termMapper);

    private Mappers() {
    }

    public static Mappers getInstance() {
        return INSTANCE;
    }

    public TermMapper term() {
        return termMapper;
    }

    public BeliefSnapshotMapper belief() {
        return beliefSnapshotMapper;
    }

    public PlanSnapshotMapper plan() {
        return planSnapshotMapper;
    }

    public IntentionSnapshotMapper intention() {
        return intentionSnapshotMapper;
    }

    public MessageSnapshotMapper message() {
        return messageSnapshotMapper;
    }
}
