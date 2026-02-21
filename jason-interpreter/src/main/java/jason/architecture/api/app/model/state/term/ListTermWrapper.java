package jason.architecture.api.app.model.state.term;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class ListTermWrapper extends TermWrapper {

    private final List<TermWrapper> terms;

    public ListTermWrapper(List<TermWrapper> terms, SourceInfoWrapper src) {
        super(src);
        this.terms = terms;
        super.setType(TermType.list);
    }
}
