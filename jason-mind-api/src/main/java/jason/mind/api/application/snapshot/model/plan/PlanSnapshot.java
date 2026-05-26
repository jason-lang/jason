package jason.mind.api.application.snapshot.model.plan;

import jason.mind.api.application.snapshot.model.SourceInfoWrapper;
import jason.mind.api.application.snapshot.model.term.StructureWrapper;
import jason.mind.api.application.snapshot.model.term.TermType;
import jason.mind.api.application.snapshot.model.term.TermWrapper;
import jason.mind.api.application.snapshot.model.term.TriggerWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PlanSnapshot extends StructureWrapper {

    private final TriggerWrapper trigger;

    private final List<TermWrapper> context;

    private final List<PlanDeedSnapshot> deeds;

    private final List<TermWrapper> annotations;

    @Setter
    private TriggerWrapper goalTrigger;

    public PlanSnapshot(TriggerWrapper trigger, String functor, String namespace, List<TermWrapper> context,
                       List<PlanDeedSnapshot> deeds, List<TermWrapper> annotations, SourceInfoWrapper src) {
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
