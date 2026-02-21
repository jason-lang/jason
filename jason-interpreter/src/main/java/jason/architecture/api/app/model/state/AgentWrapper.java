package jason.architecture.api.app.model.state;

import jason.architecture.api.app.model.state.intention.IntentionWrapper;
import jason.architecture.api.app.model.state.plan.PlanWrapper;
import jason.architecture.api.app.model.state.term.PredWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Data
public class AgentWrapper {

    private final String name;

    private final List<IntentionWrapper> intentions;

    private final List<PlanWrapper> runningPlans;

    private final List<BeliefWrapper> beliefs;

    private final List<PredWrapper> rules;

    private final List<PlanWrapper> allPlans;

    private final List<MessageWrapper> messageBox;

    private final Date time;

    public AgentWrapper(String name, Date time) {
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
