package jason.architecture.api.application.snapshot.model.term;

import jason.architecture.api.application.snapshot.model.SourceInfoWrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NamedValueTermWrapper extends ValueTermWrapper {

    private final String name;

    public NamedValueTermWrapper(String name, Object value, TermType type, SourceInfoWrapper src) {
        super(type, value, src);
        this.name = name;
    }

    @Override
    public String toString() {
        if (super.getValue() == null) {
            return this.name;
        } else {
            return this.name + "=" + super.getValue();
        }
    }
}
