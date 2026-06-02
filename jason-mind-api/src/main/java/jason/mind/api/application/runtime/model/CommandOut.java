package jason.mind.api.application.runtime.model;

import jason.mind.api.application.snapshot.model.plan.PlanDeedSnapshot;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandOut {

    private final PlanDeedSnapshot executed;

    private final int cycle;

}
