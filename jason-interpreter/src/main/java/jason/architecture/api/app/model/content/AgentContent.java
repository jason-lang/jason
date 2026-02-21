package jason.architecture.api.app.model.content;

import lombok.Getter;

@Getter
public class AgentContent {

    private String name;

    private String[] initialBeliefs;

    private String[] initialGoals;

    private PlanContent[] plans;

}
