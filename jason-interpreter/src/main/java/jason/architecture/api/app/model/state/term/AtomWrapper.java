package jason.architecture.api.app.model.state.term;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AtomWrapper extends TermWrapper {

    private final String functor;

    public AtomWrapper(String functor, SourceInfoWrapper src) {
        super(src);
        this.functor = functor;
        this.setType(TermType.atom);
    }

    @Override
    public String toString() {
        return this.functor;
    }
}
