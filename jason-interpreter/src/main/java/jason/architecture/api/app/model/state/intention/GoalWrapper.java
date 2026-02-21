package jason.architecture.api.app.model.state.intention;

import jason.architecture.api.app.model.state.term.TriggerWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GoalWrapper {

    private final TriggerWrapper trigger;

    private String state;

}
