package jason.architecture.api.app.model.state.term;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
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
