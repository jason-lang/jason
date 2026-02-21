package jason.architecture.api.app.model.state.intention;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IntentionWrapper {

    private final int id;

    private final List<GoalWrapper> stackGoals;

    private final String state;

    public IntentionWrapper(int id, String state) {
        this.id = id;
        this.stackGoals = new ArrayList<>();
        this.state = state;
    }
}
