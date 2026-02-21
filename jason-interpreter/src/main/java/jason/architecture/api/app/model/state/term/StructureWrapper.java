package jason.architecture.api.app.model.state.term;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(callSuper = true)
public class StructureWrapper extends AtomWrapper {

    private final String namespace;

    @Setter
    private List<TermWrapper> terms;

    public StructureWrapper(String functor, String namespace, List<TermWrapper> terms, SourceInfoWrapper src) {
        super(functor, src);
        this.namespace = namespace;
        this.terms = terms;
        this.setType(TermType.structure);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        if (this.getTerms() != null && !this.getTerms().isEmpty()) {
            builder.append(String.format("(%s)", this.getTerms().stream().map(Object::toString)
                                                     .collect(Collectors.joining(","))));
        }
        return builder.toString();
    }
}
