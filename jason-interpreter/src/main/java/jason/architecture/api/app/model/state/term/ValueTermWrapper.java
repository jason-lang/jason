package jason.architecture.api.app.model.state.term;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ValueTermWrapper extends TermWrapper {

    private final Object value;

    public ValueTermWrapper(TermType type, Object value, SourceInfoWrapper src) {
        super(src);
        this.value = value;
        this.setType(type);
    }

    @Override
    public String toString() {
        if (super.getType().equals(TermType.string.name())) {
            return "\"" + this.value.toString() + "\"";
        }
        return this.value.toString();
    }
}
