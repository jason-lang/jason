package jason.architecture.api.application.snapshot.model.intention;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IntentionSnapshot {

    private final int id;

    private final List<GoalSnapshot> stackGoals;

    private final String state;

    public IntentionSnapshot(int id, String state) {
        this.id = id;
        this.stackGoals = new ArrayList<>();
        this.state = state;
    }
}
