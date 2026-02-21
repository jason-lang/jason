package jason.architecture.api.app.model.state.plan;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import jason.architecture.api.app.model.state.term.StructureWrapper;
import jason.architecture.api.app.model.state.term.TermType;
import jason.architecture.api.app.model.state.term.TermWrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlanDeedWrapper extends StructureWrapper {

    private final TermWrapper term;

    private final String formType;

    private boolean selected;

    public PlanDeedWrapper(TermWrapper term, String functor, String formType, String namespace, SourceInfoWrapper src) {
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
