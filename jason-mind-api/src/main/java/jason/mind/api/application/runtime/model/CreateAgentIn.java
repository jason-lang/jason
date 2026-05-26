package jason.mind.api.application.runtime.model;

import lombok.Getter;

@Getter
public class CreateAgentIn {

    private String name;

    private String[] initialBeliefs;

    private String[] initialGoals;

    private CreatePlanIn[] plans;

}
