package jason.architecture.api.application.snapshot.model.plan;

import jason.architecture.api.application.snapshot.model.SourceInfoWrapper;
import jason.architecture.api.application.snapshot.model.term.StructureWrapper;
import jason.architecture.api.application.snapshot.model.term.TermType;
import jason.architecture.api.application.snapshot.model.term.TermWrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlanDeedSnapshot extends StructureWrapper {

    private final TermWrapper term;

    private final String formType;

    private boolean selected;

    public PlanDeedSnapshot(TermWrapper term, String functor, String formType, String namespace, SourceInfoWrapper src) {
        super(functor, namespace, null, src);
        this.term = term;
        this.formType = formType;
        this.setType(TermType.deed);
    }

    @Override
    public String toString() {
        return this.formType + this.term.toString();
    }
}
