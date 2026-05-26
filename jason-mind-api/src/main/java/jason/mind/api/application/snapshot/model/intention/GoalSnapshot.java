package jason.mind.api.application.snapshot.model.intention;

import jason.mind.api.application.snapshot.model.term.TriggerWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GoalSnapshot {

    private final TriggerWrapper trigger;

    private String state;

}
