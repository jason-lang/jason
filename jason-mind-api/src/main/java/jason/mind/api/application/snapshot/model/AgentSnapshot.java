package jason.mind.api.application.snapshot.model;

import jason.mind.api.application.snapshot.model.intention.IntentionSnapshot;
import jason.mind.api.application.snapshot.model.plan.PlanSnapshot;
import jason.mind.api.application.snapshot.model.term.PredWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Data
public class AgentSnapshot {

    private final String name;

    private final List<IntentionSnapshot> intentions;

    private final List<PlanSnapshot> runningPlans;

    private final List<BeliefSnapshot> beliefs;

    private final List<PredWrapper> rules;

    private final List<PlanSnapshot> allPlans;

    private final List<MessageSnapshot> messageBox;

    private final Date time;

    public AgentSnapshot(String name, Date time) {
        this.name = name;
        this.intentions = new ArrayList<>();
        this.runningPlans = new ArrayList<>();
        this.beliefs = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.allPlans = new ArrayList<>();
        this.messageBox = new ArrayList<>();
        this.time = time;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
