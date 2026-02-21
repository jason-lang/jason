package jason.architecture.api.app.model.state.plan;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import jason.architecture.api.app.model.state.term.StructureWrapper;
import jason.architecture.api.app.model.state.term.TermType;
import jason.architecture.api.app.model.state.term.TermWrapper;
import jason.architecture.api.app.model.state.term.TriggerWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PlanWrapper extends StructureWrapper {

    private final TriggerWrapper trigger;

    private final List<TermWrapper> context;

    private final List<PlanDeedWrapper> deeds;

    private final List<TermWrapper> annotations;

    @Setter
    private TriggerWrapper goalTrigger;

    public PlanWrapper(TriggerWrapper trigger, String functor, String namespace, List<TermWrapper> context,
                       List<PlanDeedWrapper> deeds, List<TermWrapper> annotations, SourceInfoWrapper src) {
        super(functor, namespace, null, src);
        this.trigger = trigger;
        this.context = context;
        this.deeds = deeds;
        this.annotations = annotations;
        this.setType(TermType.plan);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.trigger.toString());

        if (this.getContext() != null && !this.getContext().isEmpty()) {
            builder.append(String.format(": %s",
                    this.getContext().stream().map(Object::toString).collect(Collectors.joining(","))));
        }

        return builder.toString();
    }
}
