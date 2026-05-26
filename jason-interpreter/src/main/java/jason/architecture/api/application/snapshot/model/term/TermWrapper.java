package jason.architecture.api.application.snapshot.model.term;

import jason.architecture.api.application.snapshot.model.SourceInfoWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TermWrapper {

    private final SourceInfoWrapper src;

    private String type;

    public TermWrapper(SourceInfoWrapper src) {
        this.src = src;
    }

    public void setType(TermType type) {
        this.type = type.name();
    }
}
