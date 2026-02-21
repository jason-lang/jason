package jason.architecture.api.app.model.state;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class SourceInfoWrapper {

    private final int beginLine;

    private final int endLine;

}
