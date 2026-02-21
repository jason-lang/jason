package jason.architecture.api.app.model.state;

import jason.architecture.api.app.model.state.term.PredWrapper;
import lombok.Setter;

import java.util.Objects;

@Setter
public class BeliefWrapper extends PredWrapper {

    private int cycle;

    public BeliefWrapper(PredWrapper predWrapper) {
        super(predWrapper.getFunctor(), predWrapper.getNamespace(), predWrapper.getTerms(),
                predWrapper.getAnnotations(), predWrapper.getSrc());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
