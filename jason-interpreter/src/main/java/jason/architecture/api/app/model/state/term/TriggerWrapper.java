package jason.architecture.api.app.model.state.term;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TriggerWrapper extends PredWrapper {

    private final String operator;

    private final String triggerType;

    public TriggerWrapper(PredWrapper predWrapper, String operator, String type) {
        super(predWrapper.getFunctor(), predWrapper.getNamespace(), predWrapper.getTerms(), predWrapper.getAnnotations(), predWrapper.getSrc());
        this.operator = operator;
        this.triggerType = type;
        this.setType(TermType.trigger);
    }

    @Override
    public String toString() {
        return this.operator + this.triggerType + super.toString();
    }
}
