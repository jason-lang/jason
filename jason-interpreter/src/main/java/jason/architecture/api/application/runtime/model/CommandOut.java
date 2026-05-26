package jason.architecture.api.application.runtime.model;

import jason.architecture.api.application.snapshot.model.plan.PlanDeedSnapshot;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandOut {

    private final PlanDeedSnapshot executed;

    private final int cycle;

}
