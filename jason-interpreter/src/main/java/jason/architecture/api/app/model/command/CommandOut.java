package jason.architecture.api.app.model.command;

import jason.architecture.api.app.model.state.plan.PlanDeedWrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandOut {

    private final PlanDeedWrapper executed;

    private final int cycle;

}
