package jason.mind.api.application.snapshot.model.term;

import jason.mind.api.application.snapshot.model.SourceInfoWrapper;
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
