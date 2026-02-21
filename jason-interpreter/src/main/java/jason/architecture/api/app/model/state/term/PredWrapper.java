package jason.architecture.api.app.model.state.term;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PredWrapper extends StructureWrapper {

    private final List<TermWrapper> annotations;

    public PredWrapper(String functor, String namespace, List<TermWrapper> terms, List<TermWrapper> annotations,
                       SourceInfoWrapper src) {
        super(functor, namespace, terms, src);
        this.annotations = annotations;
    }

    public PredWrapper(StructureWrapper structureWrapper, List<TermWrapper> annotations, SourceInfoWrapper src) {
        super(structureWrapper.getFunctor(), structureWrapper.getNamespace(), structureWrapper.getTerms(), src);
        this.annotations = annotations;
        this.setTerms(structureWrapper.getTerms());
        this.setType(TermType.pred);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        if (this.annotations != null && !this.annotations.isEmpty()) {
            builder.append(String.format("[%s]",
                    this.annotations.stream().map(Object::toString).collect(Collectors.joining(","))));
        }
        return builder.toString();
    }
}
